(ns clojure-stack.db.core
  (:require [korma.db :refer [defdb]]
            [clojure-stack.db.config :refer [dbconfig]]))

(defdb db dbconfig)
