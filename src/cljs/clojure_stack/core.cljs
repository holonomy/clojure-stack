(ns clojure-stack.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [put! chan alts!]]
            [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-sync.core :refer [om-sync]]
            [om-sync.util :refer [tx-tag edn-xhr]]
            [clojure.data :as data]
            [cljs-uuid.core :as uuid]))

(enable-console-print!)

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})


(def app-state
  (atom {:items {}}))

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn handle-change [e data edit-key owner]
  (om/transact! data edit-key (fn [_] (.. e -target -value))))

(defn end-edit [data edit-key text owner cb]
  (om/set-state! owner :editing false)
  (om/transact! data edit-key (fn [_] text) :update)
  (when cb
    (cb text)))

(defn editable [data owner {:keys [edit-key] :as opts}]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [edit-text editing on-edit]}]
      (let [text (get data edit-key)]
        (dom/li nil
          (dom/span #js {:style (display (not editing))} text)
          (dom/input
            #js {:style (display editing)
                 :value text
                 :onChange #(handle-change % data edit-key owner)
                 :onKeyPress #(when (and (om/get-state owner :editing)
                                         (== (.-keyCode %) 13))
                                (end-edit data edit-key text owner on-edit))
                 :onBlur (fn [e]
                           (when (om/get-state owner :editing)
                             (end-edit data edit-key text owner on-edit)))})
          (dom/button
            #js {:style (display (not editing))
                 :onClick #(om/set-state! owner :editing true)}
            "Edit"))))))

(defn create-item [items owner]
  (let [item-id  (uuid/make-random)
        item-name-el (om/get-node owner "item-title")
        item-name    (.-value item-name-el)
        new-item     {:item/id item-id :item/title item-name}]
    (println new-item)
    (om/transact! items [] #(conj % new-item)
      [:create new-item])
    (set! (.-value item-name-el) "")))

(defn items-view [items owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "items"}
        (dom/h2 nil "list of items")
        (dom/div nil
          (dom/label nil "title:")
          (dom/input #js {:ref "item-title"})
          (dom/button
            #js {:onClick (fn [e] (create-item items owner))}
           "+"))
        (apply dom/ul nil
          (map
            (fn [item]
              (om/build editable item
                        {:opts {:edit-key :item/title}}))
            items))
        ))))

(defn app-view [app owner]
  (reify
    om/IWillUpdate
    (will-update [_ next-props next-state]
      (when (:err-msg next-state)
        (js/setTimeout #(om/set-state! owner :err-msg nil) 5000)))
    om/IRenderState
    (render-state [_ {:keys [err-msg]}]
      (dom/div nil
        (om/build om-sync (:items app)
          {:opts {:view items-view
                  :filter (comp #{:create :update :delete} tx-tag)
                  :id-key :item/id
                  :on-success (fn [res tx-data] (println res))
                  :on-error
                  (fn [err tx-data]
                    (reset! app-state (:old-state tx-data))
                    (om/set-state! owner :err-msg
                      "Ooops! Sorry something went wrong try again later."))}})
         (when err-msg
           (dom/div nil err-msg))))))

(let [tx-chan (chan)
      tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
  (edn-xhr
    {:method :get
     :url "/items/init"
     :on-complete
     (fn [res]
       (println res)
       (reset! app-state res)
       (om/root app-view app-state
         {:target (gdom/getElement "items")
          :shared {:tx-chan tx-pub-chan}
          :tx-listen
          (fn [tx-data root-cursor]
            (put! tx-chan [tx-data root-cursor]))}))}))
