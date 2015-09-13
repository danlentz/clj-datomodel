(ns datomodel.nrepl
  (:require [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer [cider-nrepl-handler]]))

(defn -main
  []
  (nrepl-server/start-server :port 7888 :handler cider-nrepl-handler))
