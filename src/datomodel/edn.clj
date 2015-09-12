(ns datomodel.edn
  ""
  (:require [clojure.java.io       :as io])
  (:require [clojure.pprint        :as pp])
  (:require [clojure.edn           :as edn])
  (:require [clojure.java.io       :as io])
  (:require [clojure.string        :as str])
  (:require [datomodel.util        :as util])
  (:use     [print.foo])
  (:import  (java.net  URL))
  (:import  (java.util UUID))
  (:import  (java.io   ByteArrayInputStream
                       FileInputStream
                       File)))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDN Resources
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defprotocol StringDesignator
  (as-string [x] "Stringified name of object"))

(extend-type clojure.lang.Keyword StringDesignator
             (as-string [k]
               (name k)))

(extend-type clojure.lang.Var StringDesignator
             (as-string [v]
               (util/symbolic-name-from-var v)))

(extend-type java.lang.String StringDesignator
             (as-string [s]
               s))

(extend-type java.lang.Object StringDesignator
             (as-string [s]
               (.toString s)))


(defn- maybe-append-extension [ext base]
  (or (re-matches (re-pattern (str ".*\\." (as-string ext) "$")) base)
    (str base "." (as-string ext))))

(defn edn-resource [designator]
  (io/resource
    (maybe-append-extension :edn
      (as-string designator))))

(defn edn-file [designator]
  (io/file
    (edn-resource designator)))

(defmulti edn-resource-value type)

(defmethod edn-resource-value clojure.lang.Keyword [designator]
  (util/ignore-exceptions
    (read-string
      (slurp (edn-file designator)))))



;; (edn-resource :dt)
;;  => #<URL file:/Users/dan/src/fishbones/dev/dt.edn>
;;
;; (edn-file :dt)
;;  => #<File /Users/dan/src/fishbones/dev/dt.edn>
;;
;; (edn-resource-value :t)
;;  => [[{:db/id #db/id[:db.part/db -1000052], :db/ident :t, :dt/dt :dt/dt, :dt/namespace "system", :dt/name "T", :dt/_parent :dt/dt, :db/doc "T is the abstract superclass of all datatypes", :dt/slots [:dt/dt]}] [{:dt/namespace "system", :dt/_list :t, :dt/parent :t, :db/doc "Single-dimensional aggregate of T instances", :dt/slots [:dt/items], :db/id #db/id[:db.part/db -1], :db/ident :t*, :dt/name "T[]", :dt/dt :dt/dt, :dt/_parent :dt/dt*, :dt/component :t} {:dt/namespace "system", :dt/_list #db/id[:db.part/db -1], :dt/parent :t, :db/doc "Multi-dimensional aggregate of T instances", :dt/slots [:dt/items], :db/id #db/id[:db.part/db -1000054], :db/ident :t**, :dt/name "T[][]", :dt/dt :dt/dt, :dt/_parent :dt/dt**, :dt/component #db/id[:db.part/db -1]}]]




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn config-file
  ([]
     (config-file :config))
  ([designator]
     (io/file
       (maybe-append-extension :edn
         (as-string designator)))))

(defn config-value
  ([]
     (config-value identity))
  ([designator]
     (util/ignore-exceptions
       (designator
         (edn/read-string
           (slurp (config-file)))))))

(defn config-keys []
  (util/ignore-exceptions
    (keys (edn/read-string
            (slurp (config-file))))))


;; (config-file)
;;  => #<File config.edn>

;; (config-value :required-schema)
;;  => [:dt :t :fn :any]

;; (config-keys)
;;  => (:db :required-schema :execute-tasks :metrics :nrepl)
