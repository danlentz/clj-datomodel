(defproject danlentz/clj-datomodel "0.1.0-SNAPSHOT"
  :description "A simple Datomic Metamodel"
  :url "http://github.com/danlentz/clj-datomodel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.datomic/datomic-pro "0.9.5130" :exclusions [joda-time]]
                 [datomic-schematode "0.1.0-RC3"]
;                 [org.clojure/tools.nrepl "0.2.3"]
                 [print-foo "0.5.3"]]

  :resource-paths ["schema"]

  :asciidoc     {:sources ["doc/*.adoc" "doc/data-platform/*.adoc"]
                 ;:to-dir "doc/html"
                 :toc              :left
                 :doctype          :article
                 :format           :html5
                 :extract-css      true
                 :source-highlight true})
