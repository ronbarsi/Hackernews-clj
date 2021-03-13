(ns posts-system.web.middleware.post
  (:require [posts-system.models.posts-psql :as posts]
            [posts-system.web.helpers.responses :as responses]))

(defn wrap-post [handler]
  (fn [req]
    (let [post-id (-> req :params :id)]
      (if-let [post (posts/find-by-id (Integer/parseInt post-id))]
        (handler (assoc req :post post))
        (responses/not-found {:message "post not found"})))))