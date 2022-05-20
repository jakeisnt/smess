(ns smess.chat.input
  (:require
   [smess.chat.markdown :refer [markdown-preview]]
   [smess.sockets :refer [send-msg]]
   [reagent.core :as reagent :refer [atom]]))

(defonce input-box-name "message-input-box")


(defn chat-input
  "Allow users to input text and submit it to send messages."
  [app-state reply-to]
  (let [cur-msg (atom nil)]
    (fn []
      [:div {:class "text-input"}
       (and @reply-to
         [:div {:class "reply-preview preview-box"} (str "> " (:user @reply-to) ": ") (markdown-preview (:msg @reply-to))])
       (and @cur-msg
         [:div {:class "preview-box"} (markdown-preview @cur-msg)])
       [:form
        {:on-submit (fn [x]
                      (.preventDefault x)
                      (when-let [msg @cur-msg]
                        (send-msg {:msg msg
                                   :reply-to @reply-to
                                   :user (:user @app-state)
                                   :m-type :chat}))
                      (reset! cur-msg nil)
                      (reset! reply-to nil))}
        [:div {:style {:display "flex"
                       :flex-direction "row"}}
         [:input {:type "text"
                  :value @cur-msg
                  :id input-box-name
                  :class "message-input"
                  :placeholder "Type a message..."
                  :on-change #(reset! cur-msg (-> % .-target .-value))}]
         (and @reply-to
           [:button {:class "send-message-button"
                     :onClick #(reset! reply-to nil)} "Deselect"])
         [:button {:type "submit"
                   :class "send-message-button"} "Send"]]]])))
