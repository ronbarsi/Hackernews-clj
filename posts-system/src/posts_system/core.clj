(ns posts-system.core
  (:require [posts-system.web.routes :refer [app]]
            [db.migration :as db]
            [redis.redis :as cache]
            [org.httpkit.server :as server])
  (:gen-class))

(def port (delay (Integer/parseInt (System/getenv "SERVICE_PORT"))))

(defn -main
  [& _]
  (db/migrate)
  (cache/init)
  (server/run-server app {:port @port})
  (println (str "Running webserver at http://127.0.0.1:" @port "/")))


"
TODO:
4. write makefile (make dev + make run)
5. write mocks + tests
7. add logs
8. wrap with-exception
9. add tests fixtures (redis)
10. fix db deployment K*S to work against psql
"