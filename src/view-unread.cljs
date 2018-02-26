(ns extension.view-unread
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval boxv boxh]]
            [extension.global-atoms :refer [params-atom new-feature-modal awaiting-unread-init]]
            [extension.chat-loader :refer [chat-loader get-chats load-background load-chats seen-all?]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar input-text]]
            [reagent.cookies :as cookie]))

(defn close []
  (let [chat-list (->
                   (.item (.getElementsByClassName js/document "_2xhi") 0)
                   .-children (aget 2)
                   .-children (aget 0)
                   .-children (aget 0)
                   .-children (aget 0)
                   .-children (aget 0))
        chat-list-children (.-children chat-list)]
    (doseq [i (range (.-length chat-list-children))]
      (let [child (aget chat-list-children i)
            id (.-id child)]
        (if (= id "unread-reagent-root")
          (do (aset child "style" "display: none;"))
          (do (aset child "style" "")))))))

(def chats (atom []))

(defn mark-unread [time]
  (fn []
    (js/setTimeout
     #(load-chats chats (* 1000 (+ 1 time)))
     5000)
    #_(reset! chats (map
                   #(if (= id (:id %))
                      (merge % {:unread? false})
                      %
                      )
                   @chats))
    ))

(defn ui []
  (let [c @chats
        loading? (= 0 (count c))
        unread (->> c
                    (filter :unread?)
                    (sort-by :time)
                    reverse)
        complete? (= 0 (count unread))]
    [:div
     [:div {:className "_3rh8"
            :style {:display "flex"
                    :justify-content "space-around"
                    :margin "20px"}}
      [:button {:className "_3quh _30yy _2t_ _5ixy"
                :on-click close}
       "Back To All Threads"]
      [:button {:className "_3quh _30yy _2t_ _5ixy"
                :on-click #(do (reset! chats [])
                               (load-background chats))}
       "Refresh Unread List"]
      ]
     (cond
       loading? [:div {:style {:text-align "center"}} "Loading..."]
       (and @seen-all? complete?) [:div {:style {:text-align "center"}}
                                   (str "No unreads in all chats! (loaded " (count c) " chats)")]
       complete? [:div {:style {:text-align "center"}}
                  (str "Loading.. (None so far, loaded " (count c) " chats)")]
       :else [:ul
              (map (fn [{:keys [last-message id name href img time]}]
                     ^{:key id} [:li {:className "_5l-3 _1ht1 _1ht3"}
                                 [:div {:className "_5l-3 _1ht5"}
                                  [:a {:onClick (mark-unread time)
                                       :href href
                                       :className "_1ht5 _2il3 _5l-3 _3itx"}
                                   [:div {:className "_1qt3 _5l-3"}
                                    [:div
                                     [:div {:className "_4ldz" :style {:height "50px" :width "50px"}}
                                      [:div {:className "_4ld-" :style {:height "50px" :width "50px"}}
                                       [:div {:className "_55lt" :size "50" :style {:height "50px" :width "50px"}}
                                        [:img {:height "50" :width "50" :src img}]]]]]]
                                   [:div {:className "_1qt4 _5l-m"}
                                    [:div {:className "_1qt5 _5l-3"}
                                     [:span {:className "_1ht6"} name]
                                     [:div [:abbr {:className "_1ht7 timestamp"} time]]
                                     ]
                                    [:div {:className "_1qt5 _5l-3"}
                                     [:span {:className "_1htf"} last-message]]
                                    ]]
                                  ]]) unread)])])
  )

(defn unsafe-init []
  (let [chat-list (->
                   (.item (.getElementsByClassName js/document "_2xhi") 0)
                   .-children (aget 2)
                   .-children (aget 0)
                   .-children (aget 0)
                   .-children (aget 0)
                   .-children (aget 0))
        chat-list-children (.-children chat-list)
        seen-root (atom false)]
    (doseq [i (range (.-length chat-list-children))]
      (let [child (aget chat-list-children i)
            id (.-id child)]
        (if (= id "unread-reagent-root")
          (do (aset child "style" "")
              (reset! seen-root true))
          (do (aset child "style" "display: none;")))))
    (if-not @seen-root
      (let [root (.createElement js/document "div")]
        (.setAttribute root "id" "unread-reagent-root")
        (.appendChild chat-list root)
        (reagent/render [ui] (.getElementById js/document "unread-reagent-root"))))))

(defn init []
  (reset! chats [])
  (load-background chats)
  (if (:doc_id @params-atom)
    (unsafe-init)
    (reset! awaiting-unread-init true)))
