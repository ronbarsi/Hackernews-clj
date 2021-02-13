(ns posts-system.core
  (:require [posts-system.web.routes :refer [app]]
            [mysql.db :as db]
            [redis.redis :as cache]
            [org.httpkit.server :as server])
  (:gen-class))

(def port (delay (Integer/parseInt (System/getenv "SERVICE_PORT"))))

(defn -main
  [& _]
  (db/init)
  (cache/init)
  (server/run-server app {:port @port})
  (println (str "Running webserver at http://127.0.0.1:" @port "/")))


"
currently the DB is configured in test context, but migration doesn't run so the table aren't there

TODO:
3. move to honeysql db handler + psql
4. write makefile (make dev + make run)
5. write mocks + tests
6. DB migrations
7. add logs
8. wrap with-exception
9. add tests fixtures (redis)
"