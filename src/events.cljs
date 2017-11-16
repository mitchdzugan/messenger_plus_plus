(ns extension.events
  (:require [datival.core :as dv]
            [extension.datascript :as ds]))

(defn inc-req [r]
  (-> (str "36r" r)
      cljs.reader/read-string
      inc
      (.toString 36)))

(def dispatch (->> [dv/dispatch-system
                    dv/ajax-system
                    (dv/datascript-system {} ds/conn)
                    {:sources {}
                     :events {:network-intercept (fn [_ {req :user}]

                                                   (if-let [body ((js->clj req) "body")]
                                                     {:datascript [(merge
                                                                    {:db/path [[:db/role :anchor] :root/request-params]}
                                                                    (if (every? #(contains? body %)
                                                                                ["__req" "__rev" "__pc" "__a" "__dyn" "fb_dtsg" "jazoest"])
                                                                      {:req (inc-req (first (body "__req")))
                                                                       :rev (first (body "__rev"))
                                                                       :pc (first (body "__pc"))
                                                                       :a (first (body "__a"))
                                                                       :dyn (first (body "__dyn"))
                                                                       :fb_dtsg (first (body "fb_dtsg"))
                                                                       :jazoest (first (body "jazoest"))}
                                                                      {})
                                                                    (if (= "MessengerGraphQLThreadlistFetcherRe"
                                                                           (first (body "batch_name")))
                                                                      {:doc_id (->> (body "queries")
                                                                                    first
                                                                                    (.parse js/JSON)
                                                                                    js->clj
                                                                                    (#(get % "o0"))
                                                                                    (#(get % "doc_id")))}
                                                                      {}))]}
                                                     {}))}
                     :sinks {}}]
                   (dv/make-event-system false)
                   :dispatch))


