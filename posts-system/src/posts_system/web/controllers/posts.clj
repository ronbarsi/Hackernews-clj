(ns posts-system.web.controllers.posts
  (:require [redis.redis :as cache]
            [posts-system.helpers :as helpers]
            [posts-system.models.posts :as posts]
            [posts-system.web.helpers
             [validations :as validations :refer [when-valid]]
             [responses :as response]]))

(defn create [{{:keys [content title] :as params} :params}]
  (when-valid params validations/create-post
    (let [result (posts/create title content)
          new_id (:generated_key (first result))]
      (response/created {:id new_id :message (str "created successfully with id " new_id)}))))

(defn list* []
  (let [posts (posts/list*)]
    (response/ok {:posts posts})))

(defn show [{:keys [post]}]
  (response/ok {:post post}))

(defn update* [{{:keys [content title id] :as params} :params post :post}]
  (when-valid params validations/update-post
    (let [c (or content (:content post))
          t (or title (:title post))
          creation_timestamp (:creation_timestamp post)]
      (posts/update c t id creation_timestamp)
      (response/ok {:id (:id post) :title t :content c}))))

(defn vote [{:keys [id]} vote]
  (posts/vote vote id)
  (response/ok {:post (posts/show id)}))

(defn upvote [{:keys [post]}]
  (vote post "up"))

(defn downvote [{:keys [post]}]
  (vote post "down"))

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
  (posts/delete id)
  (response/ok {:message "deleted successfully"}))
