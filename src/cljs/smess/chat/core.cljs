(ns smess.chat.core
  (:require
   [smess.chat.input :refer [chat-input]]
   [smess.chat.history :refer [chat-history]]
   [smess.chat.sidebar :refer [sidebar]]))

(defn chat-view
  "Displays all of the chat history."
  [app-state msg-list users]
  [:div {:class "chat-container"}
   [chat-history msg-list app-state]
   [chat-input app-state]
   [:div {:class "header"}
    [:h3 "smess"]]
   [sidebar users app-state]])
