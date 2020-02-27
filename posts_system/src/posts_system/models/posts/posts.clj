(ns posts_system.models.posts.posts
  (:require
   [posts_system.core :as db]
   [org.httpkit.server :as server]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.middleware.defaults :refer :all]    [clojure.pprint :as pp]
   [clojure.string :as str]
   [clojure.data.json :as json]
   [clojure.java.jdbc :as sql]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.util.response :refer [response]]
   [clj-time.coerce :as c]
   [taoensso.carmine :as car :refer (wcar)])
  (:gen-class))


