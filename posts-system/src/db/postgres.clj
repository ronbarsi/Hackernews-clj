(ns db.postgres
  (:require [honeysql.format :as fmt]
            [clojure.data.json :as json]
            [next.jdbc.result-set :as result-set]
            [next.jdbc.prepare :as prepare])
  (:import [org.postgresql.util PGobject]
           [clojure.lang IPersistentMap IPersistentVector]
           [org.postgresql.jdbc PgArray]
           [org.postgresql.geometric PGpoint]
           [java.sql Timestamp]))

(defmulti read-array-val type)
(defmethod read-array-val (Class/forName "[Ljava.lang.Double;") [val] (vec val))
(defmethod read-array-val :default [val] val)

(extend-protocol result-set/ReadableColumn
  PGobject
  (read-column-by-index [pgobj _metadata _index]
    (let [type (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (json/read-str value :key-fn keyword)
        "jsonb" (json/read-str value :key-fn keyword)
        value)))

  PgArray
  (read-column-by-index [pgobj _metadata _index]
    (let [v (vec (.getArray pgobj))]
      (map read-array-val v)))

  PGpoint
  (read-column-by-index [pgobj _metadata _index]
    (let [x (.x pgobj)
          y (.y pgobj)]
      {:x x :y y})))

(defn to-pg-json
  [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/write-str value))))

(extend-protocol prepare/SettableParameter
  IPersistentMap
  (set-parameter [v s i]
    (.setObject s i (to-pg-json v)))

  IPersistentVector
  (set-parameter [v s i]
    (.setObject s i (to-pg-json v))))

(defmethod fmt/fn-handler "@>" [_ field val]
  (str (fmt/to-sql field) " @> ARRAY[" (fmt/to-sql-value (int val)) "]"))

(defmethod fmt/fn-handler "@@" [_ field val]
  (format "%s @@ '%s'" (name field) val))

; extends java.sql.Timestamp to write as string when encoding to JSON
(extend-type Timestamp
  json/JSONWriter
  (-write [date out]
    (json/-write (str date) out)))