(ns smess.chat.input
  (:require
   [smess.chat.markdown :refer [markdown-preview]]
   [smess.sockets :refer [send-msg]]
   [rum.core :as rum]))

(def ^:const input-box-name "message-input-box")
(defonce cur-msg (atom ""))

(rum/defc chat-input < rum/reactive
  "Allow users to input text and submit it to send messages."
  [app-state reply-to]
      [:.text-input
       (and (rum/react reply-to)
         [:div {:class "reply-preview preview-box"} (str "> " (:user @reply-to) ": ") (markdown-preview (:msg @reply-to))])
       (and @cur-msg (not (= @cur-msg ""))
         [:.preview-box (markdown-preview @cur-msg)])
       [:form
        {:on-submit (fn [event]
                      (.preventDefault event)
                      (when-let [msg @cur-msg]
                        (println reply-to)
                        (send-msg {:msg msg
                                   :reply-to (:id @reply-to)
                                   :user (:user @app-state)
                                   :m-type :chat}))
                      (reset! cur-msg "")
                      (reset! reply-to nil))}
        [:.flexrow
         [:input {:type "text"
                  :value (rum/react cur-msg)
                  :id input-box-name
                  :class "message-input"
                  :placeholder "Type a message..."
                  :on-change (fn [e] (reset! cur-msg (.. e -target -value)))}]
         (and (rum/react reply-to)
           [:button {:class "send-message-button"
                     :on-click #(reset! reply-to nil)} "Deselect"])
         [:button {:type "submit"
                   :class "send-message-button"} "Send"]]]])
