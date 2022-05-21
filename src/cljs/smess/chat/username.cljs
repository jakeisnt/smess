(ns smess.chat.username
  (:require
    [rum.core :as rum]))

(rum/defc username-box < rum/reactive
  "An interactive box containing the username."
  [username app-state]
  [:p {:key username
       :class (str "username" (and (= (:user (rum/react app-state)) username) " my-username"))}
   (if (= (:user (rum/react app-state)) username)
     (str "me [ " username " ]")
     username)])
