(ns smess.login
  (:require
   [smess.utils :refer [ormap]]
   [smess.cookies :refer [cookie->clj! add-cookie!]]
   [smess.notifications :refer [enable-notifications]]
   [smess.sockets :refer [add-user!]]
   [reagent.core :as reagent :refer [atom]]))


;; steps:
;; - poll the websocket channel to see all of the users
;; - determine if the username string is in them
;; - disallow it if it is


(defn get-invalid-username-error
  "Gets the error associated with an invalid username if there is one."
  [val users]
  (println users)
  (cond
    (or (= val "") (nil? val)) "Use a non-empty username."
    (ormap (partial = " ") (.split val "")) "The username should not include spaces."
    (some (fn [] (partial = val)) users) "That username has already been taken."
    :else nil))

(defn login-view
  "Allows users to pick a username and enter the chat."
  [app-state msg-list users]
  (let [v (atom (:username (cookie->clj!)))
        notif-error (atom nil)]
    (fn []
      [:div {:class "login-container"}
       [:form
        {:class "login"
         :on-submit (fn [x]
                      (.preventDefault x)
                       ;; if the user exists, they can enter the application.
                      (let
                       [username-error (get-invalid-username-error @v @users)]
                        (if (not username-error)
                          (do
                            (swap! app-state assoc :user @v)
                            (swap! app-state assoc :active-panel :chat)
                            (add-user! @v)
                            (add-cookie! {:username @v :samesite "Strict"}))
                          (reset! notif-error username-error))))}
        [:input {:type "text"
                 :class "username-input"
                 :value @v
                 :placeholder "Pick a username"
                 :on-change #(let
                              [val (-> % .-target .-value)]
                               (reset! v val)
                               (reset! notif-error (get-invalid-username-error val users)))}]
        [:button {:type "submit"
                  :onClick enable-notifications
                  :class "button-primary start-chatting-button"} "Start chatting"]]
       [:div {:class "error-tip-container"} (if @notif-error [:div {:class "error-tip"} @notif-error] nil)]])))
