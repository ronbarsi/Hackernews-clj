(ns mysql.db
  (:require
    [clojure.data.json :as json]
    [clojure.java.jdbc :as sql])
  (:gen-class))

(def ^:dynamic mysql-db {:classname "com.mysql.jdbc.Driver"
                         :dbtype    "mysql"
                         :dbname    "db"
                         :user      "root"
                         :password  "pp"
                         :host      "mydb"
                         :port      "3306"
                         :useSSL    false})

(def posts_table-ddl (sql/create-table-ddl :posts
                                           [[:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
                                            [:title "varchar(1000)" "NOT NULL"]
                                            [:content :text "NOT NULL"]
                                            [:upvote "int" "DEFAULT 0"]
                                            [:downvote "int" "DEFAULT 0"]
                                            [:creation_timestamp :timestamp "NOT NULL" "DEFAULT NOW()"]]
                                           [[:opt-un [:clojure.java.jdbc.spec/conditional? true]]]))

(def funct-ddl (str "CREATE FUNCTION db.post_score("
                    " upvote int,"
                    " downvote int,"
                    " creation_timestamp timestamp,"
                    " now timestamp)"
                    " RETURNS int"
                    " DETERMINISTIC"
                    " RETURN (upvote - downvote - TIMESTAMPDIFF(MINUTE, creation_timestamp, now));"))

(def num-of-top-posts 30)
(def top-posts-query
  (str "SELECT id, title, content, upvote, downvote, creation_timestamp FROM ("
       " SELECT *, post_score(upvote, downvote, creation_timestamp, NOW()) as score"
       " FROM posts"
       " ORDER BY score DESC"
       " LIMIT " num-of-top-posts
       " ) as res"))

(extend-type java.sql.Timestamp
  json/JSONWriter
  (-write [date out]
    (json/-write (str date) out)))

(defn db-init
  ([] (println "\nInitialize DB") (db-init 1))
  ([c]
   (if (= c 50) (throw (Exception. (str "Cant connect to DB. Max retries " c)))
                (do (try (sql/query mysql-db ["SELECT 'ping'"])
                         (println "Creating posts table")
                         (try (sql/db-do-commands mysql-db [posts_table-ddl])
                              (catch java.sql.BatchUpdateException se (println "\t-Table already exists")))
                         (println "Creating top-posts function")
                         (try (sql/db-do-commands mysql-db [funct-ddl])
                              (catch java.sql.BatchUpdateException se (println "\t-Function already exists")))
                         (println "DB Initialized successfully\n")
                         (catch Exception e (do (Thread/sleep 5000) (println "Waiting for DB......... retries: " c) (db-init (+ 1 c)))))))))