(ns redis.redis
  (:require
   [taoensso.carmine :as car :refer (wcar)])
  (:gen-class))


(def redis_url (System/getenv "REDIS_URL"))

(def server1-conn {:pool {} :spec {:uri redis_url}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn update_cache [key value]
  (wcar* (car/set key value)))

(defn get_cache [key]
  (wcar* (car/get key)))

(defn update_top_posts [value]
  (update_cache "top_posts" value))

(defn update_last_execution [value]
  (update_cache "last_execution" value))

(defn get_top_posts []
  (get_cache "top_posts"))

(defn get_last_execution []
  (get_cache "last_execution"))

(defn cache-init []
  (println "Initialize cache:")
  (update_top_posts "")
  (update_last_execution (str (- (quot (System/currentTimeMillis) 1000) 120)))
  (println "Cache Initialized successfully\n"))