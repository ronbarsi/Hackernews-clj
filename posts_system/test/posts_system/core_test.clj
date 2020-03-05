(ns posts_system.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [mysql.db :as db]
            [posts_system.models.posts.posts :refer [get-post]]
            [posts_system.core :refer [app-routes]]
            [posts_system.controllers.posts :as post-controller]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]))

(def mysql-db-test {:classname "com.mysql.jdbc.Driver"
                    :dbtype    "mysql"
                    :dbname    "db"
                    :user      "root"
                    :password  "pp"
                    :host      "localhost"
                    :port      "3308"
                    :useSSL    false})


(deftest create-post-test
  (binding [db/mysql-db mysql-db-test]
    (testing "post creation"
      (let [response (app-routes
                       (-> (mock/request :post "/posts")
                           (mock/content-type "application/json")
                           (mock/json-body {:content "c1" :title "t1"})))
            body (json/read-str (:body response))
            data (get body "data")
            message (get body "message")
            id (get data "id")
            post (get-post id)]
        (is (= (:status response) 201))
        (is (= message (str "created successfully with id " id)))

        (testing "inserted to db"
          (is post)
          (is (= (:id post) id)))))
    (testing "post creation without content"
      (let [bad_response (app-routes
                           (-> (mock/request :post "/posts")
                               (mock/content-type "application/json")
                               (mock/json-body {:title "t1"})))
            body (json/read-str (:body bad_response))
            message (get body "message")]
        (is (= (:status bad_response) 400))
        (is (= message "content or title are missing"))))))