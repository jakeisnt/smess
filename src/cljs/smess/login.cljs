(ns smess.login
  (:require
    [smess.utils :refer [ormap]]
    [smess.sockets :refer [setup-websockets!]]
    [smess.cookies :refer [cookie->clj! add-cookie!]]
    [smess.notifications :refer [enable-notifications]]
    [rum.core :as rum]))

(defn get-invalid-username-error
  "Gets the error associated with an invalid username if there is one."
  [val]
  (cond
    (or (= val "") (nil? val)) "Use a non-empty username."
    (ormap (partial = " ") (.split val "")) "The username should not include spaces."
    :else nil))

(rum/defcs login-view
  < rum/reactive
    (rum/local (:username (or (cookie->clj!) "")) ::username)
    (rum/local nil ::notif-error)
  "Allows users to pick a username and enter the chat."
  [state app-state msg-list users]
  (let [username (::username state)
        notif-error (::notif-error state)]
    [:.login-container
     [:form
      {:class "login"
       :on-submit (fn [e]
                    (.preventDefault e)
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
                :on-click enable-notifications
                :class "button-primary start-chatting-button"} "Start chatting"]]
     [:.error-tip-container (and @notif-error [:div {:class "error-tip"} @notif-error])]]))
