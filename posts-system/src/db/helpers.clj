(ns db.helpers
  (:require [honeysql
             [helpers :as sql]
             [format :refer [value]]
             [types :as types]]
            [clj-time
             [core :as t]
             [coerce :as c]])
  (:import [honeysql.types SqlCall]))

(defn- format-inner-array [v]
  (if (vector? v)
    (types/array v)
    v))

(defn- format-values [v]
  (reduce-kv
    (fn [acc k v]
      (let [fv (cond
                 (instance? SqlCall v) v
                 (map? v) (value v)
                 (vector? v) (->> v
                                  (map format-inner-array)
                                  types/array)
                 :else v)]
        (assoc acc k fv)))
    {}
    v))

(defn values [q & vs] (sql/values q (map format-values vs)))

(defn sql-time-now [] (-> (t/now) (c/to-sql-time)))