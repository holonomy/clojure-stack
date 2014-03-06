(ns clojure-stack.db.config
  (:require [korma.db :refer [defdb h2]]))

(def dev
  (h2 {:db "resources/db/dev.db"}))

(def dbconfig dev)
