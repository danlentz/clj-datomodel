(ns datomodel.state
  (:require [com.stuartsierra.component :as component])
  (:require [clojure.tools.namespace.repl :refer (refresh refresh-all)])
  (:require [datomodel.nrepl :as nrepl])
  (:require [datomodel.edn :as dedn])
  (:require [datomodel.util :as util]))

(def state (atom {:db-spec      nil
                  :nrepl-server nil
                  :schema       []
                  :started    false}))

(defn loaded-schema []
  (:schema @state))

(defn set-db-spec! [url sid]
  (util/returning-bind [spec (str url sid)]
    (swap! state assoc :db-spec spec)))

(defn clear-db-spec! []
  (swap! state assoc :db-spec nil))

(defn db-spec []
  (or (:db-spec @state)
    (apply set-db-spec!
      ((juxt :url :sid)
       (dedn/config-value :db)))))

(defn nrepl-server
  []
  (:nrepl-server @state))

(defn started?
  []
  (:started @state))
