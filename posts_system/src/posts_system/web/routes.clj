(ns posts-system.web.routes
  (:require [posts_system.web.controllers.posts :as posts]
            [compojure.core :refer [GET PUT POST DELETE defroutes]]
            [compojure.route :refer [not-found]]
            [ring.middleware
             [json :refer [wrap-json-params wrap-json-response]]
             [content-type :refer [wrap-content-type]]
             [keyword-params :refer [wrap-keyword-params]]
             [params :refer [wrap-params]]]))

(defroutes all
  (GET "/ping" [] "PONG")
  (GET "/posts/top" [] (posts/top))
  (GET "/posts" [] (posts/list*))
  (POST "/posts" req (posts/create req))
  (GET "/posts/:id" [id] (posts/show id))
  (PUT "/posts/:id" req (posts/update* req))
  (DELETE "/posts/:id" [id] (posts/delete id))
  (PUT "/posts/:id/up" [id] (posts/upvote id))
  (PUT "/posts/:id/down" [id] (posts/downvote id))
  (not-found "Page not found"))

(def app
  (-> all
      wrap-content-type
      wrap-keyword-params
      wrap-params
      wrap-json-params
      wrap-json-response))
