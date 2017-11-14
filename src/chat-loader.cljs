(ns extension.chat-loader
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval boxv boxh]]
            [extension.global-atoms :refer [params-atom new-feature-modal]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar]]
            [reagent.cookies :as cookie]))

(defn get-chats []
  (map (fn [li]
         {:id (-> li
                  .-children (aget 0)
                  (.getAttribute "id"))
          :name (-> li
                    .-children (aget 0)
                    .-children (aget 0)
                    .-children (aget 1)
                    .-children (aget 0)
                    .-children (aget 0)
                    .-innerHTML)
          :time (-> li
                     .-children (aget 0)
                     .-children (aget 0)
                     .-children (aget 1)
                     .-children (aget 0)
                     .-children (aget 1)
                     .-children (aget 0)
                     (.getAttribute "data-utime"))
          :muted? (-> li
                      (.getAttribute "class")
                      (clojure.string/includes? "_569x"))})
       (.from js/Array (-> js/document
                           (.querySelector "ul[aria-label='Conversation List']")
                           .-children))))

(defn get-scroll-area []
  (-> js/document
      (.getElementsByClassName "_1enh") (aget 0)
      .-children (aget 0)
      .-children (aget 1)
      .-lastChild
      .-children (aget 0)
      .-children (aget 0)))

(defn get-scroll-height []
  (.-scrollTop (get-scroll-area)))

(defn set-scroll-height [height]
  (set! (.-scrollTop (get-scroll-area)) height))

(defn set-max-scroll-height []
  (set-scroll-height (.-scrollHeight (get-scroll-area))))


(def chat-search-interval (reagent/atom false))
(def last-scroll-height (reagent/atom 0))
(def user-request-more (reagent/atom false))
(def load-for-user (reagent/atom true))

(defn make-chat-search-interval [chats]
  (js/setInterval
   (fn []
     (if (or @user-request-more
             (and @load-for-user
                  (< (- (.getTime (js/Date.))
                        (* 1000 (->> @chats (map :time) sort first)))
                     2592000000)))
       (do (if @chat-search-interval
             (set-max-scroll-height))
           (reset! chats
                   (vals
                    (merge
                     (reduce merge {} (map (fn [chat] {(:id chat) chat}) (get-chats)))
                     (reduce merge {} (map (fn [chat] {(:id chat) chat}) @chats))))))
       (do (js/clearInterval @chat-search-interval)
           (reset! chat-search-interval false)
           (set-scroll-height @last-scroll-height))))
   1000))

(defn chat-loader [chats]
  (reagent/create-class
   {:component-did-mount (fn []
                           (reset! load-for-user true)
                           (reset! last-scroll-height (get-scroll-height))
                           (reset! chat-search-interval (make-chat-search-interval chats)))
    :component-will-unmount (fn []
                              (js/clearInterval @chat-search-interval)
                              (reset! chat-search-interval false)
                              (set-scroll-height @last-scroll-height))
    :reagent-render (fn []
                      [:div {:style {:border-width "1px 0 1px 0"
                                     :border-style "solid"
                                     :border-color "#ddd"
                                     :padding "5px"
                                     :margin "5px"}}
                       (if (and @load-for-user
                                (< (- (.getTime (js/Date.))
                                      (* 1000 (->> @chats (map :time) sort first)))
                                   2592000000))
                         [:div
                          [:button {:className "_3quh _30yy _2t_ _5ixy"
                                    :style {:font-size "12px"
                                            :width "85px"}
                                    :on-click (fn [] (reset! load-for-user false))}
                           "Stop Loading"]
                          [:p {:style {:display "inline"}}
                           "Loading chats active in the last month... (at "
                           [:b (->> @chats (map :time) sort first (* 1000) js/Date. .toLocaleDateString)]
                           " so far)"]]
                         [:div
                          [:button {
                                    :className "_3quh _30yy _2t_ _5ixy"
                                    :style {:font-size "12px"
                                            :width "85px"}
                                    :on-click (fn []
                                                (if @user-request-more
                                                  (do (reset! user-request-more false)
                                                      (js/clearInterval @chat-search-interval)
                                                      (reset! chat-search-interval false)
                                                      (set-scroll-height @last-scroll-height))
                                                  (do (reset! user-request-more true)
                                                      (reset! chat-search-interval (make-chat-search-interval chats)))))}
                           (if @user-request-more "Stop Loading" "Load More")]
                          [:p {:style {:display "inline"}}
                           "Showing chats active since "
                           [:b (->> @chats (map :time) sort first (* 1000) js/Date. .toLocaleDateString)]]])])}))
