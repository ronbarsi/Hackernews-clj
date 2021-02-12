(ns posts-system.web.routes
  (:require [posts_system.web.controllers.posts :as posts]
            [posts-system.web.middleware.post :refer [wrap-post]]
            [compojure.core :refer [GET PUT POST DELETE defroutes context wrap-routes]]
            [compojure.route :refer [not-found]]
            [ring.middleware
             [json :refer [wrap-json-params wrap-json-response]]
             [content-type :refer [wrap-content-type]]
             [keyword-params :refer [wrap-keyword-params]]
             [params :refer [wrap-params]]]))

(defroutes unrestricted-routes
  (context "/posts" []
    (GET  "/top" []  (posts/top))
    (GET  "/"    []  (posts/list*))
    (POST "/"    req (posts/create req))))

(defroutes post-restricted-routes
  (context "/posts" []
    (GET    "/:id"      req (posts/show req))
    (PUT    "/:id"      req (posts/update* req))
    (DELETE "/:id"      req (posts/delete req))
    (PUT    "/:id/up"   req (posts/upvote req))
    (PUT    "/:id/down" req (posts/downvote req))))

(defroutes all
  (GET "/ping" [] "PONG")
  unrestricted-routes
  (wrap-routes post-restricted-routes wrap-post)
  (not-found "Page not found"))

(def app
  (-> all
      wrap-content-type
      wrap-keyword-params
      wrap-params
      wrap-json-params
      wrap-json-response))
