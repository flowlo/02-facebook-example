(defproject facebook-example "0.1.0-SNAPSHOT"
  :description "Facebook Messenger Bot in Clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" "/home/lorenz/.m2"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.1"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [environ "1.1.0"]
                 [clj-time "0.13.0"]
                 [clojure-term-colors "0.1.0-SNAPSHOT"]
                 [fb-messenger "0.3.0"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]
            [lein-localrepo "0.5.3"]]
  ;:hooks [environ.leiningen.hooks]
  :ring {:handler facebook-example.web/app}
  :main facebook-example.repl
  :aot [facebook-example.web facebook-example.repl]
  :uberjar-name "facebook-example-standalone.jar")
  ; :profiles {:default [:base :dev :user]
  ;            #_:production #_{:env {:production false}}})
