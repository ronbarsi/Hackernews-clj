(ns posts_system.core
  (:require [posts-system.web.routes :refer [app]]
            [mysql.db :as db]
            [redis.redis :as cache]
            [org.httpkit.server :as server])
  (:gen-class))

(def port (delay (Integer/parseInt (System/getenv "SERVICE_PORT"))))

(defn -main
  [& _]
  (db/db-init)
  (cache/cache-init)
  (server/run-server app {:port @port})
  (println (str "Running webserver at http://127.0.0.1:" @port "/")))
