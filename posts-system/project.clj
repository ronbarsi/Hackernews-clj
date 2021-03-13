(defproject posts-system "0.1.0-SNAPSHOT"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [ring/ring-json "0.5.0"]
                 [clj-time "0.15.2"]
                 [com.taoensso/carmine "2.19.1"]
                 [ring/ring-mock "0.4.0"]
                 [org.clojure/tools.namespace "1.0.0"]
                 [cheshire "5.10.0"]
                 [com.novemberain/validateur "2.5.0"]
                 [migratus "1.2.8"]
                 [seancorfield/next.jdbc "1.1.582"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [org.postgresql/postgresql "42.1.3"]
                 [honeysql "1.0.444"]
                 [nilenso/honeysql-postgres "0.2.6"]]

  :aliases {"migrations:migrate" ["run" "-m" "db.migration/migrate"]}

  :main ^:skip-aot posts-system.core
  :target-path "target/%s"
  :repl-options {:init-ns user
                 :timeout 120000}
  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths ["profiles/dev"]}})
