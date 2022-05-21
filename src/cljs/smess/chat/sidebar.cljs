(ns smess.chat.sidebar
  (:require
   [rum.core :as rum]
   [smess.chat.username :refer [username-box]]))

(rum/defc sidebar < rum/reactive
  "Shows all of the users currently in the channel."
  [users app-state]
  [:.sidebar
   [:marquee {:direction "right"}
    [:.user-list
     ;; TODO unique usernames? unique keys?
     (doall (for [[k v] (rum/react users)]
              [:.userlist-username
               (rum/with-key (username-box v app-state) k)]))]]])
