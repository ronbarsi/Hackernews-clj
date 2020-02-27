(ns posts_system.core
  (:require [org.httpkit.server :as server]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.defaults :refer :all]
    [clojure.pprint :as pp]
    [clojure.string :as str]
    [clojure.data.json :as json] 
    [clojure.java.jdbc :as sql]
    [ring.middleware.json :refer [wrap-json-response]]
    [ring.util.response :refer [response]]
    [clj-time.coerce :as c]
    [taoensso.carmine :as car :refer (wcar)]
    )
  (:gen-class))

(use '[ring.middleware.json :only [wrap-json-body json-body-request]] '[ring.util.response :only [response]])
(use '[ring.middleware.params :only [wrap-params params-request]])
(use '[ring.util.request :only [body-string]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;HELPERS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn timestamp->string [tmstmp] 
  (let [time (str (c/from-long (* 1000 tmstmp)))]
    (reduce 
      (fn [res curr] 
        (if (str/includes? curr "Z") 
          (str res " " (subs curr 0 10))
          (str res curr))) 
      "" 
      (str/split time #"T"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;CACHE;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def redis_url (System/getenv "REDIS_URL"))

(def server1-conn {:pool {} :spec {:uri redis_url}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn update_cache [key value]
  (wcar* (car/set key value))
)

(defn get_cache [key]
  (wcar* (car/get key))
)

(defn update_top_posts [value]
  (update_cache "top_posts" value)  
)

(defn update_last_execution [value]
  (update_cache "last_execution" value)  
)

(defn get_top_posts []
  (get_cache "top_posts")
)

(defn get_last_execution []
  (get_cache "last_execution")
)

(defn cache-init []
  (update_top_posts "")
  (update_last_execution (str (- (quot (System/currentTimeMillis) 1000) 120)))
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;DB;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mysql-db {
  :classname "com.mysql.jdbc.Driver"
  :dbtype "mysql"
  :dbname "db"
  :user "root"
  :password "pp"
  :host "mydb"
  :port "3306"})

(def posts_table-ddl (sql/create-table-ddl :posts
  [[:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
  [:title "varchar(1000)" "NOT NULL"]
  [:content :text "NOT NULL"]
  [:upvote "int" "DEFAULT 0"]
  [:downvote "int" "DEFAULT 0"]
  [:creation_timestamp :timestamp "NOT NULL" "DEFAULT NOW()"]]
  [[:opt-un [:clojure.java.jdbc.spec/conditional? true]]]))
    
(def funct-ddl (str "CREATE FUNCTION db.post_score("
  " upvote int,"
  " downvote int,"
  " creation_timestamp timestamp,"
  " now timestamp)"
  " RETURNS int"
  " DETERMINISTIC"
  " RETURN (upvote - downvote - TIMESTAMPDIFF(MINUTE, creation_timestamp, now));"))

(def num-of-top-posts 30)
(def top-posts-query 
  (str "SELECT id, title, content, upvote, downvote, creation_timestamp FROM ("
          " SELECT *, post_score(upvote, downvote, creation_timestamp, NOW()) as score"
          " FROM posts"
          " ORDER BY score DESC"
          " LIMIT " num-of-top-posts
        " ) as res"))

(defn db-init []
  (println "waiting for db")
  (Thread/sleep 2000)

  (extend-type java.sql.Timestamp
    json/JSONWriter
    (-write [date out]
    (json/-write (str date) out)))

  (try (
    (println (sql/db-do-commands mysql-db
    [posts_table-ddl])))
  (catch java.sql.BatchUpdateException se (println "Table already exists")))

  (try (
    (println (sql/db-do-commands mysql-db
      [funct-ddl])) 
    )
  (catch java.sql.BatchUpdateException se (println "Function already exists")))

)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;ENDPOINTS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn err-response [msg status] 
  { :status status
  :headers {"Content-Type" "text/json"}
  :body (->> (json/write-str {:data "" :message msg}))
  })

(defn not-found-response [msg] (err-response msg 404))
(defn bad-request-response [msg] (err-response msg 400))


(defn list-all-posts [req]
  {:status  200
  :headers {"Content-Type" "application/json"}
  :body    (->> (json/write-str {:data (sql/query mysql-db ["SELECT * FROM db.posts "]) :message ""}))})


(defn create-new-post [req]
  (let [body (:body req)
        content (get body "content")
        title (get body "title")]
        (if (or (nil? content) (nil? title))
          (bad-request-response "content or title are missing")
          {:status 201
          :headers {"Content-Type" "application/json"}
          :body (->>
                  (let [response  (sql/insert! mysql-db :posts {:title title :content content })
                        new_id (:generated_key (first response))] 
                        (json/write-str {:data {:id new_id} :message (str "created successfully with id " new_id)})))})))


(defn delete-post [req] 
  {
    :status 200
    :headers {"Content-Type" "application/json"}
    :body (->>
      (let [id (Integer/parseInt (:id (:params req)))]
          (let [res (first (sql/delete! mysql-db :posts ["id = ?" id]))] 
            (if (> res 0) 
              (json/write-str {:data "" :message "deleted successfully"})
              (json/write-str {:data "" :message "nothing to delete!"})))))})


(defn get-single-post [req] 
  (let [id (Integer/parseInt (:id (:params req)))]
    (let [res (sql/get-by-id mysql-db :posts id)]
      (if (nil? res)
        (not-found-response (str "post " id " wasn't found"))
        {:status  200
        :headers {"Content-Type" "application/json"}
        :body    (->> (json/write-str {:data res :message ""}))}))))


(defn update-post [req] 
  (let [body (:body req)
        content (get body "content")
        title (get body "title")
        id (Integer/parseInt (:id (:params req)))
        post (sql/get-by-id mysql-db :posts id)]
          (if (not post) 
            (not-found-response (str "post " id " wasn't found"))
            (if (and (nil? content) (nil? title))
              (bad-request-response "content or title to update are missing")
              {
                :status  200
                :headers {"Content-Type" "application/json"}
                :body    (->> (let [c (if (nil? content) (:content post) content)
                                    t (if (nil? title) (:title post) title)
                                    res (sql/update! mysql-db :posts {:content c :title t} ["id = ?" id])]
                                  (json/write-str {:data {:id (:id post) :title t :content c} :message ""})))}))))

(defn vote [req vote]
  (let [id (Integer/parseInt (:id (:params req)))]
    (let [res (first (sql/execute! mysql-db [(str "UPDATE posts SET " vote "vote = " vote "vote + 1, creation_timestamp = creation_timestamp WHERE id = ?" ) id]))]
      (if (= res 0)
        (not-found-response (str "post " id " wasn't found"))
        {:status  200
        :headers {"Content-Type" "application/json"}
        :body    (->> (json/write-str {:data (sql/get-by-id mysql-db :posts id) :message ""}))}))))

(defn upvote [req] (vote req "up"))
(defn downvote [req] (vote req "down"))


(defn top-posts [req] 
  (let [execution-timestamp (quot (System/currentTimeMillis) 1000)
        last_execution (Integer/parseInt (get_last_execution))]
    (if (< (- execution-timestamp last_execution) 60)
      (get_top_posts) 
      (let 
        [
          result (sql/query mysql-db [top-posts-query])
          execution-timestamp-str (timestamp->string execution-timestamp)
          response {
            :status  200
            :headers {"Content-Type" "application/json"}
            :body (->> 
              (json/write-str {:last_execution execution-timestamp-str :data result :message ""}))}
        ]
        (do 
          (update_last_execution execution-timestamp)
          (update_top_posts response)
          response)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;ROUTS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-routes
  (GET "/posts" [] list-all-posts)
  (POST "/posts" [] (wrap-json-body create-new-post))
  (GET "/posts/get/:id" [id] get-single-post)
  (PUT "/posts/:id" [id] (wrap-json-body update-post))
  (DELETE "/posts/:id" [id] delete-post)
  (PUT "/posts/up/:id" [id] upvote)
  (PUT "/posts/down/:id" [id] downvote)
  (GET "/posts/top" [] top-posts)
  (route/not-found "Error, page not found!"))


(defn -main
  [& args]
  (let [port (Integer/parseInt "3000")]

    (db-init)
    (cache-init)
    (server/run-server #'app-routes {:port port})
    
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
