(ns smess.chat.reply)

(defn message-reply
  ;; A reply to a previous message.
  [msg]
  [:div {:class "message-reply"} (str "> " (:user msg) ": " (:msg msg))])
