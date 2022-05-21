(ns smess.chat.sidebar
  (:require
   [rum.core :as rum]
   [smess.chat.username :refer [username-box]]))

(rum/defc sidebar < rum/reactive
  "Shows all of the users currently in the channel."
  [users app-state]
  [:div {:class "sidebar"}
   [:marquee {:direction "right"}
    [:div {:class "user-list"}
     (doall (for [[k v] (rum/react users)]
              [:div {:class "userlist-username"}
               ^{:key k} (username-box v app-state)]))]]])
