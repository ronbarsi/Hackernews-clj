(ns mysql.db
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as sql]))

(def funct-ddl (str "CREATE FUNCTION db.post_score("
                    " upvote int,"
                    " downvote int,"
                    " creation_timestamp timestamp,"
                    " now timestamp)"
                    " RETURNS int"
                    " DETERMINISTIC"
                    " RETURN (upvote - downvote - TIMESTAMPDIFF(MINUTE, creation_timestamp, now));"))

(def num-of-top-posts (System/getenv "TOP_POSTS_PAGE_SIZE"))

(def top-posts-query
  (str "SELECT id, title, content, upvote, downvote, creation_timestamp FROM ("
       " SELECT *, post_score(upvote, downvote, creation_timestamp, NOW()) as score"
       " FROM posts"
       " ORDER BY score DESC"
       " LIMIT " num-of-top-posts
       " ) as res"))
