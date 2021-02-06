(ns redis.redis
  (:require [taoensso.carmine :as car :refer (wcar)]))


(def redis_url (System/getenv "REDIS_URL"))

(def server1-conn {:pool {} :spec {:uri redis_url}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn update_cache [key value]
  (wcar* (car/set key value)))

(defn get_cache [key]
  (wcar* (car/get key)))

(defn update-top-posts [value]
  (update_cache "top_posts" value))

(defn update-last-execution [value]
  (update_cache "last_execution" value))

(defn get-top-posts []
  (get_cache "top_posts"))

(defn get_last_execution []
  (get_cache "last_execution"))

(defn cache-init
  ([] (println "Initialize cache") (cache-init 1))
  ([c]
   (if (= c 50) (throw (Exception. (str "Cant connect to Redis. Max retries " c)))
                (do (try (wcar* (car/ping))
                         (update-top-posts "")
                         (update-last-execution (str (- (quot (System/currentTimeMillis) 1000) 120)))
                         (println "Cache Initialized successfully\n")
                         (catch Exception e (Thread/sleep 10000) (println "Waiting for Redis......... retries: " c) (cache-init (+ 1 c))))))))