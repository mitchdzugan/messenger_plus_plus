(ns extension.bulk-mute
  (:require [reagent.core :as reagent :refer [atom]]
            [extension.css :refer [my-css]]
            [extension.settings-pane :refer [add-to-pane]]
            [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval boxv boxh]]
            [extension.global-atoms :refer [params-atom new-feature-modal]]
            [extension.chat-loader :refer [chat-loader get-chats]]
            [ajax.core :refer [GET POST]]
            [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar input-text]]
            [reagent.cookies :as cookie]))

(defn set-mute
  ([id do-mute?] (set-mute id do-mute? 4 (fn [])))
  ([id do-mute? x] (if (fn? x)
                     (set-mute id do-mute? 4 x)
                     (set-mute id do-mute? x (fn []))))
  ([id do-mute? mute-length on-complete]
   (POST (str "https://"
              (.-host js/location)
              "/ajax/mercury/change_mute_thread.php?dpr=2")
         {:format :url
          :params {"thread_fbid" (last (clojure.string/split id #":"))
                   "mute_settings" (if do-mute? -1 0)
                   "payload_source" "mercury"
                   "__user" (cookie/get "c_user")
                   "__a" (:a @params-atom)
                   "__dyn" (:dyn @params-atom)
                   "__af" (:af @params-atom)
                   "__b" -1
                   "__pc" (:pc @params-atom)
                   "__rev" (:rev @params-atom)
                   "fb_dtsg" (:fb_dtsg @params-atom)
                   "jazoest" (:jazoest @params-atom)
                   "__req" (:req @params-atom)
                   }
          :handler on-complete})))


(defn get-li-container [element]
  (cond (not element) element
        (= "LI" (.-tagName element)) element
        :else (get-li-container (.-parentNode element))))


(def chats (reagent/atom []))
(def chat-name-filter (reagent/atom ""))

(def selections (reagent/atom {:last-single false
                               :owner false
                               :drop-target false
                               :items []}))

(defn add-selection [item]
  (if (or (not (:owner @selections))
          (= (:owner @selections) (.-parentNode item)))
    (do (.setAttribute item "aria-grabbed" "true")
        (reset! selections {:last-single item
                            :owner (.-parentNode item)
                            :drop-target (:drop-target @selections)
                            :items (conj (:items @selections) item)}))))

(defn add-many [item]
  (let [parent (.-parentNode item)
        last-single (:last-single @selections)]
    (if (and last-single
             (= (:owner @selections) (.-parentNode item)))
      (let [siblings (->> (.from js/Array (.-children parent))
                          (drop-while (fn [n] (and (not= n last-single)
                                                   (not= n item))))
                          reverse
                          (drop-while (fn [n] (and (not= n last-single)
                                                   (not= n item))))
                          reverse)]
        (doseq [n siblings]
          (.setAttribute n "aria-grabbed" "true"))
        (reset! selections {:last-single (:last-single @selections)
                            :owner (:owner @selections)
                            :drop-target (:drop-target @selections)
                            :items (into [] (into #{} (concat (:items @selections) siblings)))})))))

(defn remove-selection [item]
  (.setAttribute item "aria-grabbed" "false")
  (reset! selections {:last-single (:last-single @selections)
                      :owner (:owner @selections)
                      :drop-target (:drop-target @selections)
                      :items (remove #(= item %) (:items @selections))}))

(defn clear-selections []
  (doseq [item (:items @selections)]
    (.setAttribute item "aria-grabbed" "false"))
  (reset! selections {:drop-target (:drop-target @selections)
                      :last-single false
                      :owner false
                      :items []}))

(defn has-ctrl-modifier [e]
  (or (.-ctrlKey e) (.-metaKey e)))

(defn has-shift-modifier [e]
  (.-shiftKey e))

(defn targets [] (.from js/Array (.querySelectorAll js/document "[data-draggable='target']")))
(defn items [] (.from js/Array (.querySelectorAll js/document "[data-draggable='item']")))

(defn set-defaults []
  (doseq [target (targets)]
    (if-not (.hasAttribute target "aria-dropeffect")
      (.setAttribute target "aria-dropeffect" "none")))
  (doseq [item (items)]
    (if-not (.hasAttribute item "draggable")
      (.setAttribute item "draggable" "true"))
    (if-not (.hasAttribute item "aria-grabbed")
      (.setAttribute item "aria-grabbed" "false"))
    (if-not (.hasAttribute item "tab-index")
      (.setAttribute item "tab-index" "0"))))

(defn add-drop-effects []
  (doseq [target (targets)]
    (if (and (= target (:owner @selections))
             (= "none" (.getAttribute target "aria-dropeffect")))
      (do (.setAttribute target "aria-dropeffect" "move")
          (.setAttribute target "tabindex" "0"))))
  (doseq [item (items)]
    (if (and (= (.-parentNode item) (:owner @selections))
             (.getAttribute item "aria-grabbed"))
      (do (.removeAttribute item "aria-grabbed")
          (.removeAttribute item "tabindex")))))

(defn clear-drop-effects []
  (if (> (count (:items @selections)) 0)
    (do (doseq [target (targets)]
          (if-not (= "none" (.getAttribute target "aria-dropeffect"))
            (do (.setAttribute target "aria-dropeffect" "none")
                (.removeAttribute target "tabindex"))))
        (doseq [item (items)]
          (cond
            (not (.getAttribute item "aria-grabbed"))
            (do (.setAttribute item "aria-grabbed" "false")
                (.setAttribute item "tabindex" "0"))
            (= "true" (.getAttribute item "aria-grabbed"))
            (.setAttribute item "index" "0"))))))

(defn get-container [element]
  (cond (not element) element
        (and (= 1 (.-nodeType element))
             (.getAttribute element "aria-dropeffect"))
        element
        :else (get-container (.-parentNode element))))

(.addEventListener js/document "mousedown"
                   (fn [e]
                     (if-let [target (get-li-container (.-target e))]
                       (cond
                         (.getAttribute target "draggable")
                         (do (clear-drop-effects)
                             (if (and (not (or (has-ctrl-modifier e) (has-shift-modifier e)))
                                      (not= "true" (.getAttribute target "aria-grabbed")))
                               (do (clear-selections)
                                   (add-selection target))))
                         (not (or (has-ctrl-modifier e) (has-shift-modifier e)))
                         (do (clear-drop-effects)
                             (clear-selections))
                         :else (clear-drop-effects))))
                   false)

(.addEventListener js/document "mouseup"
                   (fn [e]
                     (if-let [target (get-li-container (.-target e))]
                       (if (and (.getAttribute target "draggable")
                                (or (has-ctrl-modifier e) (has-shift-modifier e)))
                         (if (= "true" (.getAttribute target "aria-grabbed"))
                           (do (remove-selection target)
                               (if (> (count (:items @selections)) 0)
                                 (reset! selections {:items []
                                                     :last-single false
                                                     :owner false
                                                     :drop-target (:drop-target @selections)})))
                           (if (or (has-ctrl-modifier e)
                                   (not (:last-single @selections)))
                             (add-selection target)
                             (add-many target))))))
                   false)

(.addEventListener js/document "dragstart"
                   (fn [e]
                     (let [target (get-li-container (.-target e))]
                       (if (= (:owner @selections) (.-parentNode target))
                         (do (if (and (or (has-ctrl-modifier e) (has-shift-modifier e))
                                      (= "false" (.getAttribute target "aria-grabbed")))
                               (add-selection target))
                             (.setData (.-dataTransfer e) "text" "")
                             (add-drop-effects))
                         (.preventDefault e))))
                   false)

(def related (reagent/atom false))


(.addEventListener js/document "dragenter"
                   (fn [e]
                     (reset! related (.-target e)))
                   false)

(.addEventListener js/document "dragleave"
                   (fn [e]
                     (let [drop-target (get-container @related)
                           drop-target (if-not (= drop-target (:owner @selections)) drop-target)]
                       (if-not (= drop-target (:drop-target @selections))
                         (do
                           (if (:drop-target @selections)
                             (set! (.-className (:drop-target @selections))
                                   (clojure.string/replace (.-className (:drop-target @selections)) #" dragover" "")))
                           (if drop-target
                             (set! (.-className drop-target)
                                   (str (.-className drop-target) " dragover")))
                           (reset! selections {:last-single (:last-single @selections)
                                               :drop-target drop-target
                                               :items (:items @selections)
                                               :owner (:owner @selections)})))))
                   false)

(.addEventListener js/document "dragover"
                   (fn [e]
                     (if (> (count (:items @selections)) 0)
                       (.preventDefault e)))
                   false)

(defn move-chats [destination]
  (reset! chats (reduce (fn [chats chat1]
                          (conj chats
                                (if (first (filter (fn [chat2]
                                                     (= (:id chat1) (.getAttribute chat2 "data-id")))
                                                   (:items @selections)))
                                  (merge chat1 {:destination destination})
                                  chat1))) [] @chats)))

(.addEventListener js/document "dragend"
                   (fn [e]
                     (if-let [drop-target (:drop-target @selections)]
                       (do (case (.getAttribute drop-target "id")
                             "list-unmuted" (move-chats :unmuted)
                             "list-muted" (move-chats :muted)
                             "list-temp-muted" (move-chats :temp-muted))
                           (.preventDefault e)))
                     (if (> (count (:items @selections) 0))
                       (do (clear-drop-effects)
                           (if (:drop-target @selections)
                             (do (clear-selections)
                                 (set! (.-className (:drop-target @selections))
                                       (clojure.string/replace (.-className (:drop-target @selections)) #" dragover" ""))
                                 (reset! selections {:last-single (:last-single @selections)
                                                     :drop-target nil
                                                     :items (:items @selections)
                                                     :owner (:owner @selections)})))))) false)

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


(def temp-mute-length (reagent/atom 1))
(def progress (reagent/atom {:in-progress? false
                             :total 0
                             :finished 0}))
(def percent-progress (reagent/atom 0))

(defn send-mutes [[change & changes]]
  (if change
    (set-mute (:id change)
              (not= :unmuted (:destination change))
              (if (= :temp-muted (:destination change))
                @temp-mute-length
                4)
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
                                   (reset! chats [])
                                   (reset! new-feature-modal false)
                                   ) 200))
                (reset! percent-progress (.round js/Math (/ (* (:finished @progress) 100)
                                                            (:total @progress))))
                (send-mutes changes)))))


(defn modal-body []
  (reagent/create-class
   {:component-did-mount (fn [] (set-defaults))
    :component-did-update set-defaults
    :reagent-render (fn []
                      (println [:chat-name-filter @chat-name-filter])
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
                        [:h2 {:style {:text-align "center"}} "Bulk Mute"]]
                       [chat-loader chats]
                       [boxh {:style {:border-width "0px 0 1px 0"
                                      :border-style "solid"
                                      :border-color "#ddd"
                                      :padding "0 5px 5px 5px"
                                      :margin "0 5px 5px 5px"}}
                        [:div {:style {:padding "2px 5px 0 0"}} "Filter Chats: "]
                        [input-text
                         :model chat-name-filter
                         :on-change #(reset! chat-name-filter %)
                         :change-on-blur? false]]
                       [boxh {:box {:size "1"} :size "1"}
                        [boxv {:box {:size "1"} :size "1"}
                         [:h3 {:style {:text-align "center"}} "Unmuted"]
                         [:ul {:id "list-unmuted"
                               :style {:height "100%"}
                               :data-draggable "target"}
                          (for [chat (->> @chats
                                          (filter #(or (and (nil? (:destination %)) (not (:muted? %)))
                                                       (= :unmuted (:destination %))))
                                          (filter #(or (:muted? %)
                                                       (nil? @chat-name-filter)
                                                       (= "" @chat-name-filter)
                                                       (clojure.string/includes?
                                                        (clojure.string/lower-case (:name %))
                                                        (clojure.string/lower-case @chat-name-filter))))
                                          (sort-by :time)
                                          (group-by :muted?)
                                          (#(concat (get % false) (get % true)))
                                          reverse)]
                            ^{:key (:id chat)} [:li {:data-id (:id chat)
                                                     :data-draggable "item"
                                                     :style {:font-weight (if (:muted? chat) "bold" "normal")
                                                             :overflow "hidden"
                                                             :white-space "nowrap"
                                                             :text-overflow "ellipsis"}
                                                     :dangerouslySetInnerHTML {:__html (:name chat)}}])]]
                        [boxv {:box {:size "0 0 2px"} :style {:width "1px" :height "90%" :background-color "#ddd"}} ""]
                        [boxv {:box {:size "1"} :size "1"}
                         [:h3 {:style {:text-align "center"}} "Muted"]
                         [:ul {:id "list-muted"
                               :style {:height "100%"}
                               :data-draggable "target"}
                          (for [chat (->> @chats
                                          (filter #(or (and (nil? (:destination %)) (:muted? %))
                                                       (= :muted (:destination %))))
                                          (filter #(or (not (:muted? %))
                                                       (nil? @chat-name-filter)
                                                       (= "" @chat-name-filter)
                                                       (clojure.string/includes?
                                                        (clojure.string/lower-case (:name %))
                                                        (clojure.string/lower-case @chat-name-filter))))
                                          (sort-by :time)
                                          (group-by :muted?)
                                          (#(concat (get % true) (get % false)))
                                          reverse)]
                            ^{:key (:id chat)} [:li {:data-id (:id chat)
                                                     :data-draggable "item"
                                                     :style {:font-weight (if-not (:muted? chat) "bold" "normal")
                                                             :overflow "hidden"
                                                             :white-space "nowrap"
                                                             :text-overflow "ellipsis"}
                                                     :dangerouslySetInnerHTML {:__html (:name chat)}}])]]
                        [boxv {:box {:size "0 0 2px"} :style {:width "1px" :height "90%" :background-color "#ddd"}} ""]
                        [boxv {:box {:size "1"} :size "1"}
                         [:h3 {:style {:text-align "center"}} "Temporary Mute"]
                         [boxh {:box {:size "1 1 auto" :style {:margin "8px"}} :size "1 1 auto" :align :center :gap "5px"}
                          [label :label "Temp Mute Length: "]
                          [single-dropdown
                           :choices [{:id 0 :label "30 minutes"}
                                     {:id 1 :label "1 hour"}
                                     {:id 2 :label "8 hours"}
                                     {:id 3 :label "24 hours"}]
                           :model temp-mute-length
                           :on-change #(reset! temp-mute-length %)]]
                         [:ul {:id "list-temp-muted"
                               :style {:height "100%"}
                               :data-draggable "target"}
                          (for [chat (reverse (sort-by :time (filter #(= :temp-muted (:destination %)) @chats)))]
                            ^{:key (:id chat)} [:li {:data-id (:id chat)
                                                     :data-draggable "item"
                                                     :style {:font-weight "bold"
                                                             :overflow "hidden"
                                                             :white-space "nowrap"
                                                             :text-overflow "ellipsis"}
                                                     :dangerouslySetInnerHTML {:__html (:name chat)}}])]]]
                       [boxh {:box {:size "initial"} :size "initial" :gap "15px" :style {:margin-top "7px"}}
                        [:button {:className "_3quh _30yy _2t_ _5ixy"
                                  :on-click (fn []
                                              (let [changes (filter (fn [chat]
                                                                      (and (:destination chat)
                                                                           (or (= :temp-muted (:destination chat))
                                                                               (and (= :unmuted (:destination chat))
                                                                                    (:muted? chat))
                                                                               (and (= :muted (:destination chat))
                                                                                    (not (:muted? chat)))))) @chats)]
                                                (if (> (count changes) 0)
                                                  (do (reset! progress {:in-progress? true
                                                                        :total (count changes)
                                                                        :finished 0})
                                                      (doseq [changes (partition-all (/ (count changes) 3) changes)]
                                                        (send-mutes changes))
                                                      ))))}
                         "apply"]
                        [:button {:className "_3quh _30yy _2t_ _5ixy"
                                  :on-click (fn []
                                              (reset! chats (map (fn [chat] (dissoc chat :destination)) @chats)))}
                         "discard"]]])}))

(defn open-modal []
  (reset! chats (get-chats))
  (reset! new-feature-modal modal-body))
