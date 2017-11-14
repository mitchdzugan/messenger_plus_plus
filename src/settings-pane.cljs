(ns extension.settings-pane
  (:require [extension.utils :refer [make-click remove-my-css add-my-temp-css set-interval]]))

(defn pane-item-inner-html
  [text]
  (str
   "<a class='_54nc' role='menuitem'>
     <span>
       <span class='_54nh'>" text "</span>
     </span>
   </a>"))

(defn add-to-pane
  [items]
  (add-my-temp-css "._5v-0 {display: none}")
  (let [set-interval (partial set-interval 100)
        settings-button (-> js/document
                            (.querySelector "a[aria-label='Settings, help and more']"))]
    (make-click settings-button)
    (set-interval
     (fn [i]
       (if settings-button
         (do
           (doseq [[id text on-click] items]
             (let [settings-item (.createElement js/document "li")
                   settings-menu (-> settings-button
                                     (.getAttribute "aria-controls")
                                     (#(.getElementById js/document %))
                                     (#(if %
                                         (-> % .-children (aget 0) .-children (aget 0))
                                         (let [dummy-item (.createElement js/document "div")
                                               dummy-child (.createElement js/document "div")]
                                           (.appendChild dummy-item dummy-child)
                                           (.appendChild dummy-item dummy-child)
                                           dummy-item))))]
               (.setAttribute settings-item "id" id)
               (.setAttribute settings-item "class" "_54ni __MenuItem")
               (.setAttribute settings-item "role" "presentation")
               (set! (.-onclick settings-item) (fn []
                                                 (on-click)
                                                 (make-click settings-button)))
               (set! (.-onmouseenter settings-item) (fn [] (.setAttribute
                                                            (.getElementById js/document id)
                                                            "class" "_54ni __MenuItem _54ne selected")))
               (set! (.-onmouseleave settings-item) (fn [] (.setAttribute
                                                            (.getElementById js/document id)
                                                            "class" "_54ni __MenuItem")))
               (set! (.-innerHTML settings-item) (pane-item-inner-html text))
               (.insertBefore settings-menu settings-item (-> settings-menu .-children (aget 1)))))
           (js/clearInterval i)
           (make-click settings-button)
           (remove-my-css))
         (js/clearInterval i))))))
