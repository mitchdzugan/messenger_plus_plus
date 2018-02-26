(ns extension.chat-loader
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.datascript :as ds]
            [extension.events :as events]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval boxv boxh]]
            [extension.global-atoms :refer [params-atom new-feature-modal]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar]]
            [reagent.cookies :as cookie]
            [datival.core :as dv]))

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
          :time (.floor js/Math (-> li
                                   .-children (aget 0)
                                   .-children (aget 0)
                                   .-children (aget 1)
                                   .-children (aget 0)
                                   .-children (aget 1)
                                   .-children (aget 0)
                                   (.getAttribute "data-utime")))
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
(def last-scroll-height (reagent/atom false))
(def user-request-more (reagent/atom false))
(def load-for-user (reagent/atom true))
(def seen-all? (reagent/atom false))

(defn load-chats
  ([chats] (load-chats chats false))
  ([chats oldest-override]
   (let [sorted-chats (->> @chats (sort-by :time))
         oldest-seconds (->> sorted-chats first :time)
         oldest (if oldest-seconds (* 1000 oldest-seconds))
         chats-map (->> @chats (map #(-> [(:id %) %])) (into {}))]
     (if (or oldest-override
             @user-request-more
             (and @load-for-user
                  (or (not oldest)
                      (< (- (.getTime (js/Date.))
                            oldest)
                         2592000000))))
       (if (:doc_id @params-atom)
         (do
           (if-not (= false @last-scroll-height)
             (do
               (set-scroll-height @last-scroll-height)
               (reset! last-scroll-height false)))
           (POST (str "https://"
                      (.-host js/location)
                      "/api/graphqlbatch/")
                 {:format :url
                  :response-format :raw
                  :params {"batch_name" "MessengerGraphQLThreadlistFetcher"
                           "__user" (cookie/get "c_user")
                           "__a" (:a @params-atom)
                           "__dyn" (:dyn @params-atom)
                           "__b" -1
                           "__pc" (:pc @params-atom)
                           "__rev" (:rev @params-atom)
                           "fb_dtsg" (:fb_dtsg @params-atom)
                           "jazoest" (:jazoest @params-atom)
                           "__req" (:req @params-atom)
                           "queries" (.stringify
                                      js/JSON
                                      (clj->js
                                       {:o0 {:doc_id (:doc_id @params-atom)
                                             :query_params {:limit 99
                                                            :before (if oldest-override oldest-override oldest)
                                                            :tags ["INBOX"]
                                                            :includeDeliveryReceipts true
                                                            :includeSeqID false}}}))}
                  :handler (fn [res]
                             (let [threads (->> res
                                                reverse
                                                (drop-while #(not= % "{"))
                                                (drop 1)
                                                reverse
                                                (reduce +)
                                                (.parse js/JSON)
                                                js->clj
                                                (#(get % "o0"))
                                                (#(get % "data"))
                                                (#(get % "viewer"))
                                                (#(get % "message_threads"))
                                                (#(get % "nodes"))
                                                (mapcat #(let [person? (get-in % ["thread_key" "other_user_id"])
                                                               timestamp (.floor js/Math (/ (js/parseInt (get % "updated_time_precise") 10) 1000))
                                                               other-people (->> (get-in % ["all_participants" "nodes"])
                                                                                 (map (fn [p] (get p "messaging_actor")))
                                                                                 (remove (fn [p]
                                                                                           (= (get p "id") (str (cookie/get "c_user"))))))
                                                               id (if person?
                                                                    person?
                                                                    (get-in % ["thread_key" "thread_fbid"]))
                                                               href (if person?
                                                                      (get
                                                                       (->> (get-in % ["all_participants" "nodes"])
                                                                            (map (fn [p] (get p "messaging_actor")))
                                                                            (filter (fn [p] (= id (get p "id"))))
                                                                            first) "username")
                                                                      id)
                                                               href (if (= href "")
                                                                      (get
                                                                       (->> (get-in % ["all_participants" "nodes"])
                                                                            (map (fn [p] (get p "messaging_actor")))
                                                                            (filter (fn [p] (= id (get p "id"))))
                                                                            first) "id")
                                                                      href)
                                                               img (get-in
                                                                    (->> (get-in % ["all_participants" "nodes"])
                                                                         (map (fn [p] (get p "messaging_actor")))
                                                                         (filter (fn [p] (= id (get p "id"))))
                                                                         first)
                                                                    ["big_image_src" "uri"])
                                                               name (if person?
                                                                      (get
                                                                       (->> (get-in % ["all_participants" "nodes"])
                                                                            (map (fn [p] (get p "messaging_actor")))
                                                                            (filter (fn [p] (= id (get p "id"))))
                                                                            first) "name")
                                                                      (if (get % "name")
                                                                        (get % "name")
                                                                        (let [[[first-to-name & to-name] others] (split-at 3 other-people)
                                                                              plural? (> (count others) 1)
                                                                              to-name-string (reduce (fn [s p]
                                                                                                       (str s ", " (get p "name")))
                                                                                                     (get first-to-name "name")
                                                                                                     to-name)
                                                                              others-string (if (> (count others) 0)
                                                                                              (str ", "
                                                                                                   (count others)
                                                                                                   " other"
                                                                                                   (if plural? "s" ""))
                                                                                              "")]
                                                                          (str to-name-string others-string))))
                                                               unread? (not= 0 (get % "unread_count"))
                                                               last-message (get-in % ["last_message" "nodes" 0 "snippet"])
                                                               nickname (if person?
                                                                          (get
                                                                           (->> (get-in % ["customization_info" "participant_customizations"])
                                                                                (filter (fn [p] (= id (get p "participant_id"))))
                                                                                first)
                                                                           "nickname"))]
                                                           (conj (if person?
                                                                   []
                                                                   (map (fn [person]
                                                                          {:id (get person "id")
                                                                           :name (get person "name")
                                                                           :person? true
                                                                           :time timestamp}
                                                                          ) other-people))
                                                                 {:id id
                                                                  :img img
                                                                  :name name
                                                                  :nickname nickname
                                                                  :person? person?
                                                                  :friend? true
                                                                  :time timestamp
                                                                  :unread? unread?
                                                                  :last-message last-message
                                                                  :href href
                                                                  :muted? (not (nil? (get % "mute_until")))})))
                                                )]
                               (if (< (count (filter :friend? threads)) 99)
                                 (do (reset! load-for-user false)
                                     (reset! seen-all? true)
                                     (reset! user-request-more false)))
                               (->> threads
                                    (reduce #(let [c (get %1 (:id %2))
                                                   res
                                                   (merge %1
                                                          {(:id %2) (merge c
                                                                           %2
                                                                           {:time (max (:time c) (:time %2))})})]
                                               res) chats-map)
                                    vals
                                    (map #(merge % {:name (or (:nickname %) (:name %))}))
                                    (reset! chats))
                               (js/setTimeout #(load-chats chats) 500)))}))
         (js/setTimeout #(load-chats chats) 100))
       (js/setTimeout #(load-chats chats) 1000)))))

(defn chat-loader [chats]
  (reagent/create-class
   {:component-did-mount (fn []
                           (reset! load-for-user true)
                           (reset! user-request-more true)
                           (if-not (:doc_id @params-atom)
                             (do
                               (reset! last-scroll-height (get-scroll-height))
                               (set-max-scroll-height)))
                           (load-chats chats))
    :component-will-unmount (fn []
                              (reset! user-request-more false))
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
                          (if (= 0 (count @chats))
                            [:p {:style {:display "inline"}} "Loading first batch of chats..."]
                            [:p {:style {:display "inline"}}
                             "Loading chats active in the last month... (at "
                             [:b (->> @chats (map :time) sort first (* 1000) js/Date. .toLocaleDateString)]
                             " so far)"])]
                         [:div
                          [:button {
                                    :className "_3quh _30yy _2t_ _5ixy"
                                    :style {:font-size "12px"
                                            :width "85px"}
                                    :on-click (fn []
                                                (if @user-request-more
                                                  (reset! user-request-more false)
                                                  (reset! user-request-more true)))}
                           (if @user-request-more "Stop Loading" "Load More")]
                          (if (= 0 (count @chats))
                            [:p {:style {:display "inline"}} "Loading first batch of chats..."]
                            [:p {:style {:display "inline"}}
                             "Showing chats active since "
                             [:b (->> @chats (map :time) sort first (* 1000) js/Date. .toLocaleDateString)]])])])}))

(defn load-background [chats]
  (reset! user-request-more true)
  (reset! seen-all? false)
  (if-not (:doc_id @params-atom)
    (do
      (reset! last-scroll-height (get-scroll-height))
      (set-max-scroll-height)))
  (load-chats chats))

#_(def new-chat-loader
  (dv/make-ui ds/conn
              [{:root/chats [:chat/times]}]))
