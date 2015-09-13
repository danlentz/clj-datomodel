(ns datomodel.db
  (:require [datomic.api          :as d]
            [datomic.db           :as ddb]
            [datomic.common       :as dcm]
            [clojure.edn          :as edn]
            [clojure.java.io      :as io]
            [clojure.string       :as string]
            [clojure.tools.logging :as log]
            [datomodel.state      :as state]
            [datomodel.util       :as util]
            [datomodel.edn        :as fedn]
            [datomodel.db.fn      :as fn]
            [datomodel.db.schema  :as schema]))

(defonce initialized-latch (promise))

(def ^:dynamic *db* nil)

(defn conn
  ([]     (d/connect (state/db-spec)))
  ([spec] (d/connect spec)))


(defn- initialize-db [spec]
  (schema/load-all-schema! spec)
  (fn/load-all-dbfn  spec)
  (log/debug :event "initialized db" :spec spec))


(defn- ensure-db [spec]
  (when (d/create-database spec)
    (initialize-db spec))
  (util/returning (d/db (conn spec))
    (deliver initialized-latch true)))


(defn db
  ([]
     (or *db*
       (db (state/db-spec))))
  ([spec]
     (ensure-db spec)))


(defmacro with-db [db-instance & body]
  `(binding [*db* ~db-instance]
     ~@body))


(defn entity-id [e]
  (cond
    (number? e)      (long e)
    (associative? e) (:db/id e)))


(defn excise-entity [e-or-eid]
  @(d/transact (conn)
     [{:db/id #db/id[db.part/user]
       :db/excise (entity-id e-or-eid)}]))


(defn retract-entity [e-or-eid]
  @(d/transact (conn)
     [[:db.fn/retractEntity (entity-id e-or-eid)]]))


(defn retract-entities [& e-or-eids]
  @(d/transact (conn)
     (map #(vec [:db.fn/retractEntity (entity-id %)]) e-or-eids)))


;; TODO: this is wrong

(defn delete-db [spec]
  (swap! state/state assoc
    :db-spec nil
    :schema [])
  (log/debug :OP "Delete" :DB spec)
  (println (str "Deleting " spec))
  (d/delete-database spec))

(defn delete! []
  (delete-db (state/db-spec)))

(defn ready?
  "Predicate that returns true when the DB is ready to be used."
  []
  (and (realized? initialized-latch)
       (= @initialized-latch true)))
