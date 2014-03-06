(ns lobos.migrations
  (:refer-clojure
     :exclude
     [alter drop bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]]
               core connectivity schema config helpers)))

(defmigration add-items-table
  (up [] (create
          (table :items
            (uuid :id :primary-key)
            (varchar :title 100))))
  (down [] (drop (table :items))))
