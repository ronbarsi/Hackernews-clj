(ns posts_system.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [posts_system.db :as db]
            [posts_system.controllers.posts :as post-controller]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]))

(def mysql-db-test {:classname "com.mysql.jdbc.Driver"
                    :dbtype "mysql"
                    :dbname "db"
                    :user "root"
                    :password "pp"
                    :host "localhost"
                    :port "3308"})


(deftest create-post-test
  (binding [db/mysql-db mysql-db-test]
    (let [response (post-controller/create-new-post
                    (-> (mock/request :post "/posts")
                        (mock/content-type "application/json")
                        (assoc :body {"content" "c1", "title" "t1"})))
          body (json/read-str (:body response))
          data (get body "data")
          message (get body "message")]
      (is (= (:status response) 201))
      (is (= message (str "created successfully with id " (get data "id")))))))