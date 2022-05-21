(ns smess.chat.core
  (:require
   [rum.core :as rum]
   [smess.chat.input :refer [chat-input]]
   [smess.chat.history :refer [chat-history]]
   [smess.chat.sidebar :refer [sidebar]]))

;; the currently selected message to reply to
(defonce reply-to-message (atom nil))

(rum/defc chat-view
  "Displays all of the chat history."
  [app-state msg-list users]
  [:.chat-container
   [:.header [:h3 "SMESS"]]
   (sidebar users app-state)
   (chat-history msg-list app-state reply-to-message)
   (chat-input app-state reply-to-message)])
