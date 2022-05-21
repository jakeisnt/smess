(ns smess.chat.input
  (:require
   [smess.chat.markdown :refer [markdown-preview]]
   [smess.sockets :refer [send-msg]]
   [rum.core :as rum]))

(defonce input-box-name "message-input-box")


(rum/defcs chat-input
  < rum/reactive
    (rum/local nil ::cur-msg)
  "Allow users to input text and submit it to send messages."
  [state app-state reply-to]
  (let [cur-msg (::cur-msg state)]
      [:.text-input
       (and (rum/react reply-to)
         [:div {:class "reply-preview preview-box"} (str "> " (:user @reply-to) ": ") (markdown-preview (:msg @reply-to))])
       (and @cur-msg (not (= @cur-msg ""))
         [:.preview-box (markdown-preview @cur-msg)])
       [:form
        {:on-submit (fn [event]
                      (.preventDefault event)
                      (when-let [msg @cur-msg]
                        (send-msg {:msg msg
                                   :reply-to @reply-to
                                   :user (:user @app-state)
                                   :m-type :chat}))
                      (reset! cur-msg nil)
                      (reset! reply-to nil))}
        [:.flexrow
         [:input {:type "text"
                  :value @cur-msg
                  :id input-box-name
                  :class "message-input"
                  :placeholder "Type a message..."
                  :on-change #(reset! cur-msg (-> % .-target .-value))}]
         (and (rum/react reply-to)
           [:button {:class "send-message-button"
                     :on-click #(reset! reply-to nil)} "Deselect"])
         [:button {:type "submit"
                   :class "send-message-button"} "Send"]]]]))
