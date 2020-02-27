(ns posts_system.controllers.posts
  (:require
   [posts_system.db :as db]
   [posts_system.redis :as cache]
   [posts_system.helpers :as helpers]
   [clojure.data.json :as json]
   [clojure.java.jdbc :as sql])
  (:gen-class))


(defn err-response [msg status]
  {:status status
   :headers {"Content-Type" "text/json"}
   :body (->> (json/write-str {:data "" :message msg}))})

(defn not-found-response [msg] (err-response msg 404))
(defn bad-request-response [msg] (err-response msg 400))


(defn list-all-posts [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (->> (json/write-str {:data (sql/query db/mysql-db ["SELECT * FROM db.posts "]) :message ""}))})


(defn create-new-post [req]
  (let [body (:body req)
        content (get body "content")
        title (get body "title")]
    (if (or (nil? content) (nil? title))
      (bad-request-response "content or title are missing")
      {:status 201
       :headers {"Content-Type" "application/json"}
       :body (->>
              (let [response  (sql/insert! db/mysql-db :posts {:title title :content content})
                    new_id (:generated_key (first response))]
                (json/write-str {:data {:id new_id} :message (str "created successfully with id " new_id)})))})))


(defn delete-post [req]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (->>
          (let [id (Integer/parseInt (:id (:params req)))
                res (first (sql/delete! db/mysql-db :posts ["id = ?" id]))]
              (if (> res 0)
                (json/write-str {:data "" :message "deleted successfully"})
                (json/write-str {:data "" :message "nothing to delete!"}))))})


(defn get-single-post [req]
  (let [id (Integer/parseInt (:id (:params req)))
        res (sql/get-by-id db/mysql-db :posts id)]
    (if (nil? res)
      (not-found-response (str "post " id " wasn't found"))
      {:status  200
       :headers {"Content-Type" "application/json"}
       :body    (->> (json/write-str {:data res :message ""}))})))


(defn update-post [req]
  (let [body (:body req)
        content (get body "content")
        title (get body "title")
        id (Integer/parseInt (:id (:params req)))
        post (sql/get-by-id db/mysql-db :posts id)]
    (if (not post)
      (not-found-response (str "post " id " wasn't found"))
      (if (and (nil? content) (nil? title))
        (bad-request-response "content or title to update are missing")
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (->> (let [c (if (nil? content) (:content post) content)
                             t (if (nil? title) (:title post) title)
                             creation_timestamp (:creation_timestamp post)]
                         (sql/update! db/mysql-db :posts {:content c :title t :creation_timestamp creation_timestamp} ["id = ?" id])
                         (json/write-str {:data {:id (:id post) :title t :content c} :message ""})))}))))

(defn vote [req vote]
  (let [id (Integer/parseInt (:id (:params req)))
        res (first (sql/execute! db/mysql-db [(str "UPDATE posts SET " vote "vote = " vote "vote + 1, creation_timestamp = creation_timestamp WHERE id = ?") id]))]
    (if (= res 0)
      (not-found-response (str "post " id " wasn't found"))
      {:status  200
       :headers {"Content-Type" "application/json"}
       :body    (->> (json/write-str {:data (sql/get-by-id db/mysql-db :posts id) :message ""}))})))

(defn upvote [req] (vote req "up"))
(defn downvote [req] (vote req "down"))


(defn top-posts [req]
  (let [execution-timestamp (quot (System/currentTimeMillis) 1000)
        last_execution (Integer/parseInt (cache/get_last_execution))]
    (if (< (- execution-timestamp last_execution) 60)
      (cache/get_top_posts)
      (let
       [result (sql/query db/mysql-db [db/top-posts-query])
        execution-timestamp-str (helpers/timestamp->string execution-timestamp)
        response {:status  200
                  :headers {"Content-Type" "application/json"}
                  :body (->>
                         (json/write-str {:last_execution execution-timestamp-str :data result :message ""}))}]
        (cache/update_last_execution execution-timestamp)
        (cache/update_top_posts response)
        response))))