(ns datomodel.user
  "User REPL environment for db interaction"
  (:require [datomic.api           :as d])
  (:require [datomic.db            :as ddb])
  (:require [datomic.common        :as dcm])
  (:require [datomodel.util        :as util])
  (:require [datomodel.db          :as db])
  (:require [datomodel.db.rules    :as rules])
  (:require [datomodel.db.fn       :as fn])
  (:require [datomodel.db.datatype :as dt])
  (:require [datomodel.state       :as state])
  (:require [datomodel.edn         :as dedn])
  (:require [clojure.edn           :as edn])
  (:require [clojure.pprint        :as pp])
  (:use     [datomodel.db.rules :only [defrule clear-rulebase! all-rules]])
  (:use     [datomodel.db.fn    :only [dbfn defdbfn clear-fnbase! all-dbfn]])
  (:use     [print.foo]))


(defn foo []
  nil)
