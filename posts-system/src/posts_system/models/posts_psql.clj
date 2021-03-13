(ns posts-system.models.posts-psql
  (:require [db.core :as db]
            [db.helpers :refer [values sql-time-now]]
            [honeysql-postgres
             [helpers :refer [returning]]
             [format :refer :all]]
            [honeysql.core :as sql]
            [honeysql.helpers :as helpers :refer [from insert-into limit merge-where order-by select sset]]))

(def table-name :posts)

(def ^:private allowed-create-keys #{:content :title})

(def ^:private allowed-update-keys #{:content :title :upvote :downvote :deleted_at})

(defn- criteria
  [query params]
  (let [{:keys [id deleted]
         :or {deleted false}} params
        id (if (or (nil? id) (coll? id)) id (vector id))]
    (cond-> query
      id               (merge-where [:in :id id])
      (true? deleted)  (merge-where [:<> :deleted_at nil])
      (false? deleted) (merge-where [:= :deleted_at nil]))))

(defn- create* [params]
  (let [post (select-keys params allowed-create-keys)]
    (-> (insert-into table-name)
        (values post)
        (returning :*)
        sql/format
        db/execute-one!)))

(defn- list* [params]
  (let [{lim :limit ord :order by :by
         :or {lim 1000 ord :desc by :created_at}} params]
    (-> (select :*)
        (from table-name)
        (criteria (dissoc params :limit :by))
        (order-by [by ord])
        (limit lim)
        sql/format
        db/execute!)))

(defn- find* [params]
  (-> (select :*)
      (from table-name)
      (criteria params)
      sql/format
      db/execute!))

(defn- update* [params changes]
  (let [changes (select-keys changes allowed-update-keys)]
    (-> (helpers/update table-name)
        (sset (merge changes {:updated_at (sql-time-now)}))
        (criteria params)
        (returning :*)
        sql/format
        db/execute!)))

(defn- vote [id vote]
  (first (update* {:id id} {vote (sql/call :+ vote 1)})))

(defn- delete* [params]
  (update* params {:deleted_at (sql-time-now)}))

(defn create [title content]
  (create* {:title title :content content}))

(defn list
  ([] (list {}))
  ([params] (list* params)))

(defn find-by-id [id]
  (first (find* {:id id})))

(defn delete-by-id [id]
  (first (delete* {:id id})))

(defn update-by-id [id changes]
  (first (update* {:id id} changes)))

(defn upvote [id]
  (vote id :upvote))

(defn downvote [id]
  (vote id :downvote))

(defn top []
  (list* {})) ; todo fix!!