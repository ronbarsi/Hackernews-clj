(ns posts_system.models.posts.posts
  (:require
   [mysql.db :as db]
   [clojure.java.jdbc :as sql])
  (:gen-class))


(defn all-posts []
  (sql/query db/mysql-db ["SELECT * FROM db.posts "]))

(defn get-post [id]
  (sql/get-by-id db/mysql-db :posts id))

(defn insert-post [title content]
  (sql/insert! db/mysql-db :posts {:title title :content content}))

(defn delete-post [id]
  (sql/delete! db/mysql-db :posts ["id = ?" id]))

(defn update-post [content title id creation_timestamp]
  (sql/update! db/mysql-db :posts {:content content :title title :creation_timestamp creation_timestamp} ["id = ?" id]))

(defn vote-post [vote id]
  (sql/execute! db/mysql-db [(str "UPDATE posts SET " vote "vote = " vote "vote + 1, creation_timestamp = creation_timestamp WHERE id = ?") id]))

(defn top-posts []
  (sql/query db/mysql-db [db/top-posts-query]))