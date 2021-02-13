(ns posts-system.helpers.request
  (:require [clojure.data.json :as json]
            [ring.mock.request :as mock]
            [posts-system.web.routes :refer [app]]))

(defn add-headers [request headers]
  (reduce-kv
    (fn [req h v] (mock/header req h v))
    request headers))

(defn stringify-vals [m]
  (when (not-empty m)
    (reduce-kv (fn [m k v] (assoc m k (str v))) {} m)))

(defn mock-params [req method params]
  (if (= method :get)
    (mock/query-string req (stringify-vals params))
    (mock/body req (json/write-str params))))

(defn json-response [response]
  (try
    (update response :body #(json/read-str % :key-fn keyword))
    (catch Exception e (println "EXCEPTION PARSING JSON RESPONSE" response) response)))

(def process-request (comp json-response app))

(defn request [method path & {:keys [params headers]}]
  (-> (mock/request method path)
      (mock/content-type "application/json")
      (mock-params method params)
      (add-headers headers)
      process-request))