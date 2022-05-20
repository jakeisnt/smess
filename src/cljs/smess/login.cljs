(ns smess.login
  (:require
   [smess.utils :refer [ormap]]
   [smess.sockets :refer [setup-websockets!]]
   [smess.cookies :refer [cookie->clj! add-cookie!]]
   [smess.notifications :refer [enable-notifications]]
   [reagent.core :as reagent :refer [atom]]))

(defn get-invalid-username-error
  "Gets the error associated with an invalid username if there is one."
  [val]
  (cond
    (or (= val "") (nil? val)) "Use a non-empty username."
    (ormap (partial = " ") (.split val "")) "The username should not include spaces."
    :else nil))

(defn login-view
  "Allows users to pick a username and enter the chat."
  [app-state msg-list users]
  (let [username (atom (:username (cookie->clj!)))
        notif-error (atom nil)]
    (fn []
      [:div {:class "login-container"}
       [:form
        {:class "login"
         :on-submit (fn [event]
                      (.preventDefault event)
                       ;; if the user exists, they can enter the application.
                      (let
                       [username-error (get-invalid-username-error @username)]
                        (if (and @username (not username-error))
                          (do
                            (swap! app-state assoc :user @username)
                            (swap! app-state assoc :active-panel :chat)
                            (add-cookie! {:username @username :samesite "Strict"})
                            (enable-notifications)
                            (setup-websockets! app-state msg-list users))
                          (reset! notif-error username-error))))}
        [:input {:type "text"
                 :class "username-input"
                 :value @username
                 :placeholder "Pick a username"
                 :on-change #(let
                              [val (-> % .-target .-value)]
                               (reset! username val)
                               (reset! notif-error (get-invalid-username-error val)))}]
        [:button {:type "submit"
                  :onClick enable-notifications
                  :class "button-primary start-chatting-button"} "Start chatting"]]
       [:div {:class "error-tip-container"} (if @notif-error [:div {:class "error-tip"} @notif-error] nil)]])))
