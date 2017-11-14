(ns extension.global-atoms
  (:require [reagent.core :as reagent :refer [atom]]))


(def params-atom (reagent/atom {:req "0"}))

(def new-feature-modal (reagent/atom false))
