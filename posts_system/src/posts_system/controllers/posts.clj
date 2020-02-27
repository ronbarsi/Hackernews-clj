(ns posts_system.controllers.posts
  (:require
   [posts_system.redis :as cache]
   [posts_system.helpers :as helpers]
   [clojure.data.json :as json]
   [posts_system.models.posts.posts :as model])
  (:gen-class))

;; (defn response-wraper [handler]
;;   (fn [req] (let [response (handler req)
;;                   body (json/write-str (:body response))]
;;               (assoc response
;;                      :headers {"Content-Type" "application/json"}
;;                      :body (json/write-str body)))))

(defn response [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (->> (json/write-str body))})

(defn err-response [msg status]
  (response status {:data "" :message msg}))
(defn not-found-response [msg] (err-response msg 404))
(defn bad-request-response [msg] (err-response msg 400))


(defn list-all-posts [req]
  (response 200 {:data (model/all-posts) :message ""}))


(defn create-new-post [req]
  (let [body (:body req)
        content (get body "content")
        title (get body "title")]
    (if (or (nil? content) (nil? title))
      (bad-request-response "content or title are missing")
      (let [result  (model/insert-post title content)
            new_id (:generated_key (first result))]
        (response 201 {:data {:id new_id} :message (str "created successfully with id " new_id)})))))

;; (defn delete-post [req]
;;   {:status 200
;;    :headers {"Content-Type" "application/json"}
;;    :body (->>
;;           (let [id (Integer/parseInt (:id (:params req)))
;;                 res (first (model/delete-post id))]
;;             (if (> res 0)
;;               (json/write-str {:data "" :message "deleted successfully"})
;;               (json/write-str {:data "" :message "nothing to delete!"}))))})


;; (defn delete-post-middleware [req]
;;   {:status 200
;;    :body (->>
;;           (let [id (Integer/parseInt (:id (:params req)))
;;                 res (first (model/delete-post id))]
;;             (if (> res 0)
;;               {:data "" :message "deleted successfully"}
;;               {:data "" :message "nothing to delete!"})))})


(defn delete-post [req]
  (let [id (Integer/parseInt (:id (:params req)))
        res (first (model/delete-post id))]
    (if (> res 0)
      (response 200 {:data "" :message "deleted successfully"})
      (response 200 {:data "" :message "nothing to delete!"}))))


(defn get-single-post [req]
  (let [id (Integer/parseInt (:id (:params req)))
        res (model/get-post id)]
    (if (nil? res)
      (not-found-response (str "post " id " wasn't found"))
      (response 200 {:data res :message ""}))))


(defn update-post [req]
  (let [body (:body req)
        content (get body "content")
        title (get body "title")
        id (Integer/parseInt (:id (:params req)))
        post (model/get-post id)]
    (if (not post)
      (not-found-response (str "post " id " wasn't found"))
      (if (and (nil? content) (nil? title))
        (bad-request-response "content or title to update are missing")
        (let [c (if (nil? content) (:content post) content)
              t (if (nil? title) (:title post) title)
              creation_timestamp (:creation_timestamp post)]
          (model/update-post c t id creation_timestamp)
          (response 200 {:data {:id (:id post) :title t :content c} :message ""}))))))

(defn vote [req vote]
  (let [id (Integer/parseInt (:id (:params req)))
        res (first (model/vote-post vote id))]
    (if (= res 0)
      (not-found-response (str "post " id " wasn't found"))
      (response 200 {:data (model/get-post id) :message ""}))))

(defn upvote [req] (vote req "up"))
(defn downvote [req] (vote req "down"))


(defn top-posts [req]
  (let [execution-timestamp (quot (System/currentTimeMillis) 1000)
        last_execution (Integer/parseInt (cache/get_last_execution))]
    (if (< (- execution-timestamp last_execution) 60)
      (cache/get_top_posts)
      (let
       [result (model/top-posts)
        execution-timestamp-str (helpers/timestamp->string execution-timestamp)
        response (response 200 {:last_execution execution-timestamp-str :data result :message ""})]
        (cache/update_last_execution execution-timestamp)
        (cache/update_top_posts response)
        response))))