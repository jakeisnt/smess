(ns smess.chat.core
  (:require
   [smess.chat.input :refer [chat-input]]
   [smess.chat.history :refer [chat-history]]
   [reagent.core :as reagent :refer [atom]]
   [smess.chat.sidebar :refer [sidebar]]))

;; TODO make this nil so that i am not always replying to a message
(defonce selected-message (atom {:user "jake" :msg "this is a chat message"}))

(defn chat-view
  "Displays all of the chat history."
  [app-state msg-list users]
  [:div {:class "chat-container"}
   [chat-history msg-list app-state selected-message]
   [chat-input app-state selected-message]
   [:div {:class "header"}
    [:h3 "smess"]]
   [sidebar users app-state]])
