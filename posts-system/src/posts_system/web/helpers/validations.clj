(ns posts-system.web.helpers.validations
  (:require [validateur.validation :refer [validation-set
                                           presence-of
                                           inclusion-of
                                           validate-with-predicate
                                           format-of
                                           numericality-of
                                           validate-by]]
            [posts-system.web.helpers.responses :as responses]))

(defmacro when-valid [params validator & body]
  `(let [errors# (~validator ~params)]
     (if (empty? errors#)
       (do ~@body)
       (responses/invalid-request {:message errors#}))))

(def create-post
  (validation-set
    (presence-of #{:content :title})))

(def update-post
  (validation-set
    (presence-of #{:content :title} :any true)))