(ns posts_system.helpers
  (:require [clj-time.coerce :as c]
            [clojure.string :as str]))


(defn timestamp->string [tmstmp]
  (let [time (str (c/from-long (* 1000 tmstmp)))]
    (reduce
     (fn [res curr]
       (if (str/includes? curr "Z")
         (str res " " (subs curr 0 10))
         (str res curr)))
     ""
     (str/split time #"T"))))