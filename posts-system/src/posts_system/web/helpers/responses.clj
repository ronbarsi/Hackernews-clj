(ns posts-system.web.helpers.responses
  (:require [ring.util.response :refer [status response header]]))

(defn- attach-headers [response headers-map]
  (reduce-kv
    header
    response
    headers-map))

(defmacro defresponse [name status-code default-body]
  `(defn ~name
     ([] (~name ~default-body {}))
     ([body#] (~name body# {}))
     ([body# headers#]
      (-> (response body#)
          (status ~status-code)
          (attach-headers (merge headers# {"Content-Type" "application/json"}))))))

(defresponse ok 200 {:message "ok"})
(defresponse created 201 {:message "created"})
(defresponse bad-request 400 {:message "bad request"})
(defresponse not-found 404 {:message "not found"})
(defresponse invalid-request 422 {:message "invalid request"})
(defresponse server-error 500 {:message "internal server error"})
