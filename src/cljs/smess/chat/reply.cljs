(ns smess.chat.reply
  (:require
    [rum.core :as rum])
  )

(defn scroll-to-last-reply
  "Scroll to center the element clicked onto the page."
  [id]
  (.scrollIntoView (.getElementById js/document (str "msg-" id))
                   (clj->js {:behavior "smooth" :inline "center" :block "center"})))

(rum/defc message-reply
  ;; A reply to a previous message.
  [msg]
  [:.message-reply {:on-click #((scroll-to-last-reply (:id msg)))}
   (str "> " (:user msg) ": " (:msg msg))])
