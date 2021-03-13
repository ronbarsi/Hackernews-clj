(ns db.core
  (:require [db.init :as db-config]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(def ^:private opts {:builder-fn rs/as-unqualified-lower-maps})

(defn execute!
  ([q] (execute! q nil))
  ([q tx] (jdbc/execute! (or tx db-config/pool) q opts)))

(defn execute-one!
  ([q] (execute-one! q nil))
  ([q tx] (jdbc/execute-one! (or tx db-config/pool) q opts)))
