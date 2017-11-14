(ns extension.utils
  (:require [re-com.core :refer [v-box h-box modal-panel single-dropdown label progress-bar]]))

(defn make-click [element]
  (if element
    (.dispatchEvent element (js/MouseEvent. "click" (clj->js {:bubbles true
                                                              :cancelable true
                                                              :view js/window})))))

(defn remove-my-css []
  (if-let [el (.getElementById js/document "my-css")]
    (set! (.-outerHTML el) "")))

(defn add-my-temp-css [css]
  (remove-my-css)
  (let [el (.createElement js/document "style")]
    (set! (.-type el) "text/css")
    (.setAttribute el "id" "my-css")
    (.appendChild el (.createTextNode js/document css))
    (.appendChild (.-head js/document) el))
  (js/setTimeout remove-my-css 5000))


(defn set-interval [t f]
  (let [interval-atom (atom nil)
        interval (js/setInterval (fn [] (f @interval-atom)) t)]
    (reset! interval-atom interval)
    interval))

(defn box
  [config child]
  (apply re-com.core/box (concat (mapcat (fn [[k v]] [k v]) config)
                                 [:child child])))

(defn boxx
  [el config & children]
  (let [main (apply el (concat (mapcat (fn [[k v]] [k v]) (dissoc config :box))
                               [:children children]))]
    (if (get-in config [:box :dont-use])
      main
      (box (:box config) main))))

(defn boxv [config & children] (apply boxx v-box config children))
(defn boxh [config & children] (apply boxx h-box config children))
