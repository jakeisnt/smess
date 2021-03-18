(ns smess.chat.input
  (:require
   [smess.chat.markdown :refer [markdown-preview]]
   [smess.sockets :refer [send-msg]]
   [reagent.core :as reagent :refer [atom]]))

(defn chat-input
  "Allow users to input text and submit it to send messages."
  [app-state]
  (let [v (atom nil)]
    (fn []
      [:div {:class "text-input"}
       (if (and @v (not= "" @v))
         [:div {:class "markdown-preview-box"} (markdown-preview @v)] nil)
       [:form
        {:on-submit (fn [x]
                      (.preventDefault x)
                      ;; (if (and (= (.-keyCode x) 13) (not (.-shiftKey x)))
                      (when-let [msg @v] (send-msg {:msg msg
                                                    :user (:user @app-state)
                                                    :m-type :chat}))
                      (reset! v nil))}
        [:div {:style {:display "flex"
                       :flex-direction "row"}}
         [:input {:type "text"
                  :value @v
                  :class "message-input"
                  :placeholder "Type a message..."
                  :on-change #(reset! v (-> % .-target .-value))}]
         [:button {:type "submit"
                   :class "send-message-button"} "Send"]]]])))