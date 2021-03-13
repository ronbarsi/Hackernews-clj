(ns posts-system.web.controllers.posts-test
  (:require [clojure.test :refer :all]
            [posts-system.helpers.request :refer [request]]))

(deftest create-test
  (testing "successful request"
    (let [{status :status body :body} (request :post "/posts" :params {:content "c1" :title "t1"})]
      (is (= status 201))

      )))
