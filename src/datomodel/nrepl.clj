(ns datomodel.nrepl
  (:require [clojure.tools.nrepl.server :as nrepl-server]
            [clojure.tools.logging      :as log]
            [datomodel.edn              :as dedn]
            [cider.nrepl :refer [cider-nrepl-handler]]))


(defn -main []
  (nrepl-server/start-server
    :port (dedn/config-value :nrepl)
    :handler cider-nrepl-handler))
