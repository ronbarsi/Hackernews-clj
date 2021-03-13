(ns db.migration
  (:require [db.init :refer [pool]]
            [migratus.core :as migratus]))

(def ^:private config {:store         :database
                       :migration-dir "migrations/"
                       :db            pool})

(defn migrate [] (migratus/migrate config))

(defn create [name] (migratus/create config name))