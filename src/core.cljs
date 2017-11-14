(ns extension.core
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.bulk-mute :as bulk-mute]
            [extension.bulk-add-remove :as bulk-add-remove]
            [extension.global-atoms :refer [params-atom new-feature-modal]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar]]
            [reagent.cookies :as cookie]))

(defn inc-req [r]
  (-> (str "36r" r)
      cljs.reader/read-string
      inc
      (.toString 36)))

(defn modals []
  [:div
   (if @new-feature-modal
     [modal-panel
      :backdrop-on-click #(reset! new-feature-modal false)
      :child [@new-feature-modal]]
     [:div])])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [modals] (.getElementById js/document "reagent-root")))


(defn create-reagent-root []
  (if-not (.getElementById js/document "reagent-root")
    (let [root (.createElement js/document "div")]
      (.setAttribute root "id" "reagent-root")
      (.appendChild (.-body js/document) root))))

(defn add-css []
  (let [el (.createElement js/document "style")]
    (set! (.-type el) "text/css")
    (.appendChild el (.createTextNode js/document my-css))
    (.appendChild (.-head js/document) el)))

(defn add-new-feature-panes []
  (add-to-pane [["bulk-mute-item" "Bulk Mute" bulk-mute/open-modal]
                ["bulk-add-item" "Bulk Add To Groups" bulk-add-remove/open-modal-add]
                ["bulk-remove-item" "Bulk Remove From Groups" bulk-add-remove/open-modal-remove]]))

(defn do-init! []
  (enable-console-print!)
  (-> js/chrome (aget "extension") (aget "onMessage")
      (.addListener (fn [msg]
                      (if-let [body ((js->clj msg) "body")]
                        (reset! params-atom
                                {:req (inc-req (first (body "__req")))
                                 :rev (first (body "__rev"))
                                 :pc (first (body "__pc"))
                                 :af (first (body "__af"))
                                 :a (first (body "__a"))
                                 :dyn (first (body "__dyn"))
                                 :fb_dtsg (first (body "fb_dtsg"))
                                 :jazoest (first (body "jazoest"))})
                        (swap! params-atom update :req inc-req)))))
  (add-css)
  (create-reagent-root)
  (add-new-feature-panes)
  (set-interval 1000 (fn []
                       (if-let [bulk-mute-item (.getElementById js/document "bulk-mute-item")]
                         (if-not (.getElementById js/document (-> bulk-mute-item
                                                                  .-parentNode
                                                                  .-parentNode
                                                                  .-parentNode
                                                                  .-parentNode
                                                                  .-parentNode
                                                                  (.getAttribute "data-ownerid")))
                           (do (.removeAttribute bulk-mute-item "id")
                               (add-new-feature-panes)))
                         (add-new-feature-panes))))
  (mount-root))

(defn init! []
  (set! (.-onload js/window) do-init!))

(init!)
