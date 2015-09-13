(ns datomodel.db.datatype
  "Datatype API"
  (:require [datomic.api        :as    d]
            [datomic.db         :as  ddb]
            [datomic.common     :as  dcm]
            [clojure.pprint     :as   pp]
            [datomodel.db.rules :as rule]
            [datomodel.db.fn    :as   fn]
            [datomodel.db       :as   db])
  (:use     [datomodel.db.rules :only [defrule clear-rulebase! all-rules]]
            [datomodel.db.fn    :only [defdbfn dbfn clear-fnbase! all-dbfn]]))

(defn entity [e]
  (d/entity (db/db) e))

(defn describe
  "Returns the Concise Bounded Description (CBD) of an entity 'e'"
  [e]
  (d/touch (entity e)))

(defn all-datatypes []
  (map first
    (d/q '[:find ?dt :in $ :where
           [?e :dt/dt :dt/dt]
           [?e :db/ident ?dt]]
      (db/db))))

(defn datatype-doc [dt]
  (:db/doc (entity dt)))

(defn datatype-parents [dt]
  (:dt/parent (entity dt)))

(defn datatype-ancestors [dt]
  (let [direct-parents (datatype-parents dt)]
    (distinct
      (concat direct-parents
        (mapcat datatype-parents direct-parents)))))

(defrule direct-slot [?dt ?s]
  [?dt :dt/slots ?i]
  [?i  :db/ident ?s])

(defn datatype-direct-slots [dt]
  (:dt/slots (entity dt)))

;; (defn datatype-slots [dt]
;;   (into (reduce clojure.set/union
;;           (map datatype-direct-slots (datatype-ancestors dt)))
;;     (datatype-direct-slots dt)))

(defrule effective-slot [?dt ?s]
  [?dt :dt/slots ?i]
  [?i  :db/ident ?s])

(defrule effective-slot [?dt ?s]
  [?dt  :dt/parent ?p]
  (effective-slot ?p ?s))

(defn datatype-slots [dt]
  (set
    (map first
      (d/q '[:find ?s :in $ % ?dt :where
             (effective-slot ?dt ?s)]
        (db/db) (all-rules) dt))))


(defn slot-valuetype [dt slot]
  (ffirst
    (d/q '[:find ?t :in $ ?dt ?s :where
           [?e :dt/dt    :dt/dt]
           [?e :db/ident    ?dt]
           [?i :db/ident     ?s]
           [?e :dt/slots     ?i]
           [?i :db/valueType ?v]
           [?v :db/ident     ?t]]
      (db/db) dt slot)))

(defn slot-doc [dt slot]
  (ffirst
    (d/q '[:find ?d :in $ ?dt ?s :where
           [?e :dt/dt    :dt/dt]
           [?e :db/ident    ?dt]
           [?i :db/ident     ?s]
           [?e :dt/slots     ?i]
           [?i :db/doc      ?d]]
      (db/db) dt slot)))

(defn slot-cardinality [dt slot]
  (ffirst
    (d/q '[:find ?c :in $ ?dt ?s :where
           [?e :dt/dt      :dt/dt]
           [?e :db/ident      ?dt]
           [?i :db/ident       ?s]
           [?e :dt/slots       ?i]
           [?i :db/cardinality ?v]
           [?v :db/ident       ?c]]
      (db/db) dt slot)))

(defn slot-uniqueness [dt slot]
  (ffirst
    (d/q '[:find ?u :in $ ?dt ?s :where
           [?e :dt/dt      :dt/dt]
           [?e :db/ident      ?dt]
           [?i :db/ident       ?s]
           [?e :dt/slots       ?i]
           [?i :db/unique      ?v]
           [?v :db/ident       ?u]]
      (db/db) dt slot)))


(defn map-datatype-slots [f dt]
  (map (partial f dt) (datatype-slots dt)))

(defn slotwise [f dt]
  (let [slots (datatype-slots dt)
        vals  (map-datatype-slots f dt)]
  (zipmap slots vals)))

(defn slot-valuetypes [dt]
  (slotwise slot-valuetype dt))

(defn slot-docs [dt]
  (slotwise slot-doc dt))

(defn slot-cardinalities [dt]
  (slotwise slot-cardinality dt))

(defn slot-uniquenesses [dt]
  (slotwise slot-uniqueness dt))


(defn about [dt]
  ;; TODO: do
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Examples
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;
;; (all-datatypes)
;;  => (:job-history-event.datatype/entity
;;      :job.datatype/entity
;;      :dt/dt
;;      :workflow.datatype/entity)
;;;

;;;
;; (describe :dt/dt)
;;
;; {:db/ident :dt/dt,
;;  :db/valueType :db.type/ref,
;;  :db/cardinality :db.cardinality/one,
;;  :db/doc "A reference to the data type of an entity. Entities with
;;            this attribute are known as 'typed entities'",
;;  :dt/dt :dt/dt,
;;  :dt/namespace "system",
;;  :dt/name "Datatype",
;;  :dt/slots #{:dt/list :dt/namespace :dt/parent :dt/slots :dt/name :dt/dt :dt/component},
;;  :db/id 63}

;; (describe :workflow.datatype/entity)
;;
;; {:db/ident :workflow.datatype/entity,
;;  :db/doc "A workflow represents a programmatic state machine specifying
;;            a repeatable network of tasks and decisions.",
;;  :dt/dt :dt/dt,
;;  :dt/namespace "crawfish",
;;  :dt/name "Workflow",
;;  :dt/slots #{:workflow/symbolic-name :workflow/map :workflow/definition-name
;;              :workflow/id :dt/dt :workflow/version},
;;  :db/id 75}

;; (describe :workflow/id)
;;
;; {:db/ident :workflow/id,
;;  :db/valueType :db.type/uuid,
;;  :db/cardinality :db.cardinality/one,
;;  :db/unique :db.unique/identity,
;;  :db/doc "Universally Unique Workflow Identifier",
;;  :db/id 70}
;;;

;;;
;; (datatype-doc :job.datatype/entity)
;;  => "A job represents an instance of an individual task performed
;;      as part of an executing workflow."

;; (datatype-doc :workflow.datatype/entity)
;;  => "A workflow represents a programmatic state machine specifying
;;      a repeatable network of tasks and decisions."
;;;

;;;
;; (datatype-slots :workflow.datatype/entity)
;;  => (:workflow/id
;;      :workflow/version
;;      :workflow/definition-name
;;      :dt/dt
;;      :workflow/symbolic-name
;;      :workflow/map)

;; (datatype-slots :dt/dt)
;;  => (:dt/namespace
;;      :dt/dt
;;      :dt/parent
;;      :dt/component
;;      :dt/slots
;;      :dt/list
;;      :dt/name)
;;;

;;;
;; (slot-type :dt/dt :dt/name)
;;   => :db.type/string
;;;

;;;
;; (slot-types :job.datatype/entity)
;;  => {:job/id                :db.type/uuid,
;;      :job/parent            :db.type/ref,
;;      :job/subject           :db.type/string,
;;      :job/timeout           :db.type/long,
;;      :job/workflow          :db.type/ref,
;;      :job/current-node-name :db.type/string,
;;      :dt/dt                 :db.type/ref,
;;      :job/maintenance-mode  :db.type/boolean,
;;      :job/execution-status  :db.type/string,
;;      :job/input             :db.type/string,
;;      :job/exception         :db.type/bytes}
;;;

;;;
;; (slot-doc :dt/dt :dt/dt)
;;  => "A reference to the data type of an entity. Entities with
;;     this attribute are known as 'typed entities'"

;; (slot-doc :workflow.datatype/entity :workflow/id)
;;  => "Universally Unique Workflow Identifier"
;;;

;;;
;; (slot-docs :dt/dt)
;;  => {:dt/name "The local part or unqualified name of a data type.",
;;      :dt/list "Dynamically generated reference to a list data type.
;;               Provides support for multi-dimensional values.",
;;      :dt/slots "References to the data type fields, themselves data types.",
;;      :dt/component "Only populated for list data types.",
;;      :dt/parent "The data type(s) this data type inherits from.",
;;      :dt/dt "A reference to the data type of an entity. Entities with
;;             this attribute are known as 'typed entities'",
;;      :dt/namespace "Data types have qualified names. This is the data type
;;                    namespace."}
;;;

;;;
;; (slot-cardinality :dt/dt :dt/parent)
;;  => :db.cardinality/many

;; (slot-cardinality :job.datatype/entity :job/id)
;;  => :db.cardinality/one
;;;

;;;
;; (slot-cardinalities :dt/dt)
;;  => {:dt/name      :db.cardinality/one,
;;      :dt/list      :db.cardinality/one,
;;      :dt/slots     :db.cardinality/many,
;;      :dt/component :db.cardinality/one,
;;      :dt/parent    :db.cardinality/many,
;;      :dt/dt        :db.cardinality/one,
;;      :dt/namespace :db.cardinality/one}
;;;

;;;
;; (slot-uniqueness :workflow.datatype/entity :workflow/id)
;;  => :db.unique/identity

;; (slot-uniqueness :workflow.datatype/entity :workflow/map)
;;  => nil
;;;

;;;
;; (slot-uniquenesses :workflow.datatype/entity)
;;  => {:workflow/map             nil,
;;      :workflow/symbolic-name   nil,
;;      :dt/dt                    nil,
;;      :workflow/definition-name nil,
;;      :workflow/version         nil,
;;      :workflow/id :db.unique/identity}
;;;

;;;
;; (describe-slot :dt/dt :dt/namespace)
;;  => {:db/ident :dt/namespace,
;;      :db/doc "Data types have qualified names. This is the data
;;               type namespace.",
;;      :db/valueType :db.type/string,
;;      :db/cardinality :db.cardinality/one,
;;      :db/uniqueness nil}
;;;
