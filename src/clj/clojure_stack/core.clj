(ns clojure-stack.core
  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [korma.core :as sql]
            [clojure-stack.db.core :refer [db]]))

(defn index []
  (file-response "public/html/index.html" {:root "resources"}))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(sql/defentity items)

(defn create-item [params]
  (let [id (str (:item/id params))
        title (:item/title params)
        value {:id id :title title}]
  (sql/insert items
    (sql/values value))
  (generate-response {:status :ok})))

(defn update-item [params]
  (let [id (:item/id params)
        title (:item/title params)]
    (println id title)
    (sql/update items
      (sql/set-fields {:title title})
      (sql/where {:id (str id)}))
    (generate-response {:status :ok})))

(defn get-items []
  (sql/select items))

(defn init-items []
  (generate-response
   {:items {:url "/items"
            :coll (get-items)}}))

(defroutes routes
  (GET "/" [] (index))
  (GET "/items/init" [] (init-items))
  (POST "/items"
    {params :edn-params}
    (println params)
    (create-item params))
  (PUT "/items"
    {params :edn-params}
    (update-item params))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> routes
      wrap-edn-params))

(defonce server
  (run-jetty #'app {:port 5000 :join? false}))

(defn -main []
  server)
