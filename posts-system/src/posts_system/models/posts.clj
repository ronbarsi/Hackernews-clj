(ns posts-system.models.posts
  (:require [mysql.db :as db]
            [clojure.java.jdbc :as sql]))

(defn list* []
  (sql/query db/mysql-db ["SELECT * FROM db.posts "]))

(defn show [id]
  (sql/get-by-id db/mysql-db db/posts-table id))

(defn create [title content]
  (sql/insert! db/mysql-db db/posts-table {:title title :content content}))

(defn delete [id]
  (sql/delete! db/mysql-db db/posts-table ["id = ?" id]))

(defn update [content title id creation_timestamp]
  (sql/update! db/mysql-db db/posts-table {:content content :title title :creation_timestamp creation_timestamp} ["id = ?" id]))

(defn vote [vote id]
  (sql/execute! db/mysql-db [(str "UPDATE posts SET " vote "vote = " vote "vote + 1, creation_timestamp = creation_timestamp WHERE id = ?") id]))

(defn top-posts []
  (sql/query db/mysql-db [db/top-posts-query]))