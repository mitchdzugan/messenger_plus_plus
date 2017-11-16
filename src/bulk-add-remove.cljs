(ns extension.bulk-add-remove
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval boxv boxh]]
            [extension.global-atoms :refer [params-atom new-feature-modal]]
            [extension.chat-loader :refer [chat-loader get-chats]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar input-text]]
            [reagent.cookies :as cookie]))

(defn generateOfflineThreadingId []
  (let [ret (.now js/Date)
        value (.floor js/Math (* (.random js/Math) 4294967295))
        s (.slice (str "0000000000000000000000" (.toString value 2)) 22)
        msgs (+ (.toString ret 2) s)]
    (js/parseInt msgs, 2)))

(defn add-to-chat
  [user-id group-id cb]
  (let [offlineThreadingId (generateOfflineThreadingId)]
    (POST (str "https://"
               (.-host js/location)
               "/messaging/send/?dpr=2")
          {:format :url
           :params {"client" "mercury"
                    "action_type" "ma-type:log-message"
                    "timestamp" (.now js/Date)
                    "timestamp_absolute" "Today"
                    "timestamp_time_passed" "0"
                    "is_unread" false
                    "is_cleared" false
                    "is_forward" false
                    "is_filtered_content" false
                    "is_filtered_content_bh" false
                    "is_filtered_content_account" false
                    "is_spoof_warning" false
                    "source" "source:chat:web"
                    "source_tags[0]" "source:chat"
                    "log_message_type" "log:subscribe"
                    "status" "0"
                    "offline_threading_id" offlineThreadingId
                    "message_id" offlineThreadingId
                    "manual_retry_cnt" "0"
                    "ephemeral_ttl_mode" 0
                    "log_message_data[added_participants][0]" (str "fbid:" user-id)
                    "thread_fbid" group-id
                    "__user" (cookie/get "c_user")
                    "__a" (:a @params-atom)
                    "__dyn" (:dyn @params-atom)
                    "__b" -1
                    "__pc" (:pc @params-atom)
                    "__rev" (:rev @params-atom)
                    "fb_dtsg" (:fb_dtsg @params-atom)
                    "jazoest" (:jazoest @params-atom)
                    "__req" (:req @params-atom)
                    }
           :handler cb})))

(defn remove-from-chat
  [user-id group-id cb]
  (POST (str "https://"
             (.-host js/location)
             "/chat/remove_participants/?uid="
             user-id "&tid=" group-id "&dpr=2")
        {:format :url
         :params {"__user" (cookie/get "c_user")
                  "__a" (:a @params-atom)
                  "__dyn" (:dyn @params-atom)
                  "__b" -1
                  "__pc" (:pc @params-atom)
                  "__rev" (:rev @params-atom)
                  "fb_dtsg" (:fb_dtsg @params-atom)
                  "jazoest" (:jazoest @params-atom)
                  "__req" (:req @params-atom)
                  }
         :handler cb}))

(def chats (reagent/atom []))
(def people-selected (reagent/atom #{}))
(def last-person-selected (reagent/atom false))
(def groups-selected (reagent/atom #{}))
(def last-group-selected (reagent/atom false))
(def person-name-filter (reagent/atom ""))
(def group-name-filter (reagent/atom ""))
(def progress (reagent/atom {:in-progress? false :total 0 :finished 0}))
(def percent-progress (reagent/atom 0))

(defn apply-for-pairs
  [change-fn [[user-id group-id] & pair-set]]
  (if user-id
    (change-fn user-id group-id
               (fn []
                 (reset! progress {:in-progress? true
                                   :finished (+ 1 (:finished @progress))
                                   :total (:total @progress)})
                 (if (>= (:finished @progress)
                         (:total @progress))
                   (js/setTimeout (fn []
                                    (reset! progress {:in-progress? false
                                                      :total 0
                                                      :finished 0})
                                    (reset! percent-progress 0)
                                    (reset! new-feature-modal false)
                                    ) 200))
                 (reset! percent-progress (.round js/Math (/ (* (:finished @progress) 100)
                                                             (:total @progress))))
                 (apply-for-pairs change-fn pair-set)))))

(defn apply-changes [add?]
  (let [change-fn (if add? add-to-chat remove-from-chat)
        pairs (for [person-selected @people-selected
                    group-selected @groups-selected]
                [person-selected group-selected])]
    (if (> (count pairs) 0)
      (do
        (reset! progress {:in-progress? true :total (count pairs) :finished 0})
        (doseq [pair-set (partition-all (/ (count pairs) 3) pairs)]
          (apply-for-pairs change-fn pair-set))))))

(defn column [add? people?]
  (let [top-message (if people?
                      (str "People to " (if add? "add to" "remove from") " chats")
                      (str "Groups to " (if add? "add them to" "remove them from")))
        chats-selected (if people? people-selected groups-selected)
        last-chat-selected (if people? last-person-selected last-group-selected)
        chat-name-filter (if people? person-name-filter group-name-filter)
        filter-fn (if people? filter remove)
        id-starts-with (if people? "row_header_id_user:" "row_header_id_thread:")
        chats-list (->> @chats
                        (filter-fn :person?)
                        (filter #(or (nil? @chat-name-filter)
                                     (= "" @chat-name-filter)
                                     (clojure.string/includes?
                                      (clojure.string/lower-case (:name %))
                                      (clojure.string/lower-case @chat-name-filter))
                                     (contains? @chats-selected (:id %))))
                        (sort-by :time)
                        reverse)
        on-click (fn [e chat-id]
                   (if (.-shiftKey e)
                     (do
                       (swap! chats-selected (fn [selected-set]
                                               (set (clojure.set/union
                                                     selected-set
                                                     (->> chats-list
                                                          (map :id)
                                                          (drop-while #(and (not= % chat-id)
                                                                            @last-chat-selected
                                                                            (not= % @last-chat-selected)))
                                                          reverse
                                                          (drop-while #(and (not= % chat-id)
                                                                            (not= % @last-chat-selected))))))))
                       (reset! last-chat-selected chat-id))
                     (if (contains? @chats-selected chat-id)
                       (swap! chats-selected disj chat-id)
                       (do
                         (reset! last-chat-selected chat-id)
                         (swap! chats-selected conj chat-id)))))
        ]
    [boxv {:box {:size "1"} :size "1"}
     [:h3 {:style {:text-align "center"}} top-message]
     [boxh {:style {:border-width "1px 0 1px 0"
                    :border-style "solid"
                    :border-color "#ddd"
                    :padding "5px"
                    :margin "5px"}}
      [:div {:style {:padding "2px 5px 0 0"}} "Filter Chats: "]
      [input-text
       :model chat-name-filter
       :on-change #(reset! chat-name-filter %)
       :change-on-blur? false]]
     [:ul {:style {:height "100%" :overflow-y "auto"}}
      (doall (for [chat chats-list]
               ^{:key (:id chat)} [:li {:on-click (fn [e] (on-click e (:id chat)))
                                        :id (:id chat)
                                        :style (merge {:padding "0.2em 0.4em"
                                                       :margin "0 0 2px 0"
                                                       :line-height "1.3"
                                                       :border-radius "0.2em"
                                                       :overflow "hidden"
                                                       :white-space "nowrap"
                                                       :text-overflow "ellipsis"}
                                                      (if (contains? @chats-selected (:id chat))
                                                        {:background-color "#0084ff"
                                                         :color "#fff"}))
                                        :dangerouslySetInnerHTML {:__html (:name chat)}}]))]
     [boxh {:style {:border-width "1px 0 1px 0"
                    :border-style "solid"
                    :border-color "#ddd"
                    :padding "5px"
                    :margin "0 5px 5px 5px"}}
      [:div (str "Number selected: " (count @chats-selected))]
      [:button {:className "_3quh _30yy _2t_ _5ixy"
                :style {:font-size "12px"
                        :margin "-2px 0 0 8px"}
                :on-click (fn [] (reset! chats-selected #{}))}
       "deselect"]]]))

(defn modal-body [add?]
  [boxv {:box {:size "1" :style {:height "80vh"}} :size "1"}



                       [:div {:style {:display (if (:in-progress? @progress) "block" "none")
                                      :position "fixed"
                                      :width "100%"
                                      :height "100%"
                                      :left "0"
                                      :top "0"
                                      :z-index 10
                                      :background-color "rgba(255, 255, 255, 0.8)"}}]
                       [:div {:style {:display (if (:in-progress? @progress) "block" "none")
                                      :position "fixed"
                                      :width "40%"
                                      :left "30%"
                                      :top "40%"
                                      :text-align "center"
                                      :z-index 20}}
                        [:p (str "Finished " (:finished @progress) " of " (:total @progress) " changes...")]
                        [progress-bar
                         :model    percent-progress
                         :width    "100%"
                         :striped? true
                         :style {:width "100%"}]

                        [:button {:className "_3quh _30yy _2t_ _5ixy"
                                  :style {:margin-top "7px"}
                                  :on-click (fn []
                                              (reset! chats [])
                                              (reset! percent-progress 0)
                                              (reset! progress {:in-progress? false
                                                                :total 0
                                                                :finished 0})
                                              (reset! new-feature-modal false))} "Cancel"]
                        ]



   [boxh {:box {:size "0 1 auto"} :size "1 1 auto" :align-self :center :style {:margin-bottom "10px"}}
    [:h2 {:style {:text-align "center"}} (str "Bulk " (if add? "Add To" "Remove From") " Groups")]]
   [chat-loader chats]

   [boxh {:box {:size "1"} :size "1"}
    [column add? true]
    [boxv {:box {:size "0 0 2px"} :style {:width "1px" :height "90%" :background-color "#ddd"}} ""]
    [column add? false]]
   [boxh {:box {:size "initial"} :size "initial" :gap "15px" :style {:margin-top "7px"}}
    [:button {:className "_3quh _30yy _2t_ _5ixy"
              :on-click #(apply-changes add?)}
     "Apply"]]])

(defn open-modal-add []
  (reset! chats [])
  (reset! people-selected #{})
  (reset! new-feature-modal (fn [] (modal-body true))))

(defn open-modal-remove []
  (reset! chats [])
  (reset! people-selected #{})
  (reset! new-feature-modal (fn [] (modal-body false))))
