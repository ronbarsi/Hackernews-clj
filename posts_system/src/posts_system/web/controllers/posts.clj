(ns posts_system.web.controllers.posts
  (:require [redis.redis :as cache]
            [posts_system.helpers :as helpers]
            [posts-system.web.helpers.responses :as response]
            [posts_system.models.posts :as posts]))

(defn create [{{:keys [content title]} :params}]
  (if (or (nil? content) (nil? title))
    (response/invalid-request {:message "content or title are missing"})
    (let [result  (posts/create title content)
          new_id (:generated_key (first result))]
      (response/created {:id new_id :message (str "created successfully with id " new_id)}))))

(defn list* []
  (let [posts (posts/list*)]
    (response/ok {:posts posts})))

(defn show [{:keys [post]}]
  (response/ok {:post post}))

(defn update* [{{:keys [content title id]} :params}]
  (let [p (posts/show id)]
    (if (not p)
      (response/not-found)
      (if (and (nil? content) (nil? title))
        (response/invalid-request {:message "content or title are missing"})
        (let [c (if (nil? content) (:content p) content)
              t (if (nil? title) (:title p) title)
              creation_timestamp (:creation_timestamp p)]
          (posts/update c t id creation_timestamp)
          (response/ok {:id (:id p) :title t :content c}))))))

(defn vote [id vote]
  (let [id (Integer/parseInt id)
        res (first (posts/vote vote id))]
    (if (= res 0)
      (response/not-found)
      (response/ok {:post (posts/show id)}))))

(defn upvote [{{:keys [id]} :params}]
  (vote id "up"))

(defn downvote [{{:keys [id]} :params}]
  (vote id "down"))

(defn top []
  (let [execution-timestamp (quot (System/currentTimeMillis) 1000)
        last-execution (Integer/parseInt (cache/get_last_execution))]
    (if (< (- execution-timestamp last-execution) 60)
      (cache/get-top-posts)
      (let [result (posts/top-posts)
            execution-timestamp-str (helpers/timestamp->string execution-timestamp)
            response (response/ok {:top-posts result :last_execution execution-timestamp-str})]
        (cache/update-last-execution execution-timestamp)
        (cache/update-top-posts response)
        response))))

(defn delete [{{:keys [id]} :params}]
  (let [res (first (posts/delete id))]
    (if (> res 0)
      (response/ok {:message "deleted successfully"})
      (response/ok {:message "nothing to delete!"}))))
