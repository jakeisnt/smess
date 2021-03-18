(ns smess.chat.sidebar
  (:require
   [smess.chat.username :refer [username-box]]))

(defn sidebar
  "Shows all of the users currently in the channel."
  [users app-state]
  [:div {:class "sidebar"}
   [:div {:class "user-list"}
    (doall (for [[k v] @users]
             ^{:key k} (username-box v app-state)))]])
