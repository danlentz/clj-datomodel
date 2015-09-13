(ns datomodel.core
  (:require [com.stuartsierra.component :as component])
  (:require [datomodel.nrepl :as nrepl])
  (:require [datomodel.state :as state])
  (:require [datomodel.util :as  util]))


;; (defn new-system []
;;   (component/system-map
;;     :a-component "Add your components here"))

;; (def system nil)

;; (defn init
;;   "Constructs the current development system."
;;   []
;;   (alter-var-root #'system
;;     (constantly (new-system))))

;; (defn start
;;   "Starts the current development system."
;;   []
;;   (alter-var-root #'system component/start))

;; (defn stop
;;   "Shuts down and destroys the current development system."
;;   []
;;   (alter-var-root #'system
;;                   (fn [s] (when s (component/stop s)))))

;; (defn go
;;   "Initializes the current development system and starts it running."
;;   []
;;   (init)
;;   (start))

;; (defn reset []
;;   (stop)
;;   (refresh :after 'user/go))
