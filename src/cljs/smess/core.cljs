(ns smess.core
  (:require
    [smess.login :refer [login-view]]
    [smess.chat.core :refer [chat-view]]
    [rum.core :as rum]))

(defonce app-state (atom {:text "Hello world!"
                          :active-panel :login
                          :user "test"}))

(defonce msg-list (atom []))
(defonce users (atom {}))


(rum/defcs window < rum/reactive
  "Router for the front-end application with different views, etc."
  []
   (case (:active-panel (rum/react app-state))
     :login (login-view app-state msg-list users)
     :chat (chat-view app-state msg-list users)))


(defn mount []
  (rum/mount (window) (. js/document (getElementById "app"))))

(mount)

; (defn ^:export start [] (mount))
