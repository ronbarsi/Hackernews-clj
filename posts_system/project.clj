(defproject posts_system "0.1.0-SNAPSHOT"
  :main ^:skip-aot posts_system.core
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
  ; Compojure - A basic routing library
  [compojure "1.6.1"]
  ; Our Http library for client/server
  [http-kit "2.3.0"]
  ; Ring defaults - for query params etc
  [ring/ring-defaults "0.3.2"]
  ; Clojure data.JSON library
  [org.clojure/data.json "0.2.6"]
  [org.clojure/java.jdbc "0.7.11"]
  [mysql/mysql-connector-java "5.1.38"]
  [ring/ring-json "0.5.0"]
  [clj-time "0.15.2"]
  [com.taoensso/carmine "2.19.1"]]

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
