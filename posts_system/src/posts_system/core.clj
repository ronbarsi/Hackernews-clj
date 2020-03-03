(ns posts_system.core
  (:require
   [posts_system.db :as db]
   [posts_system.redis :as cache] 
   [posts_system.controllers.posts :as post-controller]
   [org.httpkit.server :as server]
   [compojure.core :as rest]
   [compojure.route :as route]
   [ring.middleware.json :refer [wrap-json-body]])
  (:gen-class))


(rest/defroutes app-routes
                (rest/GET "/posts/top" [] post-controller/top-posts)
                (rest/GET "/posts" [] post-controller/list-all-posts)
                (rest/POST "/posts" [] (wrap-json-body post-controller/create-new-post))
                (rest/GET "/posts/:id" [] post-controller/get-single-post)
                (rest/PUT "/posts/:id" [] (wrap-json-body post-controller/update-post))
                (rest/DELETE "/posts/:id" [] post-controller/delete-post)
                (rest/PUT "/posts/up/:id" [] post-controller/upvote)
                (rest/PUT "/posts/down/:id" [] post-controller/downvote)

                (route/not-found "Error, page not found!"))


(defn -main
  [& args]
  (let [port (Integer/parseInt "3000")]

    (db/db-init)
    (cache/cache-init)
    (server/run-server #'app-routes {:port port})

    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
