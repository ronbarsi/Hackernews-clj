(ns db.init
  (:require [db.postgres]
            [next.jdbc.connection :as connection])
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]))

(def ^:private db-spec (delay {:dbtype   "postgresql"
                               :dbname   "posts_system"
                               :host     (System/getenv "DB_HOST")
                               :user     (System/getenv "DB_USER")
                               :password (System/getenv "DB_PASSWORD")}))

(def pool (connection/->pool ComboPooledDataSource @db-spec))

