(ns posts-system.web.controllers.posts
  (:require [redis.redis :as cache]
            [posts-system.helpers :as helpers]
            [posts-system.models.posts-psql :as post]
            [posts-system.web.helpers
             [validations :as validations :refer [when-valid]]
             [responses :as response]]))

(defn create [{{:keys [content title] :as params} :params}]
  (when-valid params validations/create-post
    (let [created (post/create title content)]
      (response/created {:post created :message "post created successfully"}))))

(defn list* []
  (let [posts (post/list)]
    (response/ok {:posts posts})))

(defn show [{:keys [post]}]
  (response/ok {:post post}))

(defn update* [{{:keys [content title] :as params} :params post :post}]
  (when-valid params validations/update-post
    (let [changes (cond-> {}
                    content (assoc :content content)
                    title   (assoc :title title))
          updated (post/update-by-id (:id post) changes)]
      (response/ok updated))))

(defn upvote [{:keys [post]}]
  (response/ok {:post (post/upvote (:id post))}))

(defn downvote [{:keys [post]}]
  (response/ok {:post (post/downvote (:id post))}))

(defn top []
  (let [execution-timestamp (quot (System/currentTimeMillis) 1000)
        last-execution (Integer/parseInt (cache/get_last_execution))]
    (if (< (- execution-timestamp last-execution) 60)
      (response/ok {:top-posts (post/list {:id (cache/get-top-posts)}) :last_execution (helpers/timestamp->string last-execution)}) ;todo fix!!! (or from-cache from-db)
      (let [result (post/top)
            execution-timestamp-str (helpers/timestamp->string execution-timestamp)
            response (response/ok {:top-posts result :last_execution execution-timestamp-str})]
        (cache/update-last-execution execution-timestamp)
        (cache/update-top-posts (map :id result))
        response))))

(defn delete [{:keys [post]}]
  (let [deleted (post/delete-by-id (:id post))]
    (response/ok {:deleted deleted :message "post deleted successfully"})))
