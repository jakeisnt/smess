(ns smess.core
  (:require [reagent.core :as reagent :refer [atom]]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :as async :include-macros true]))

(def BASEURL "http://localhost:3449")
(goog-define ws-url "ws://localhost:3449/ws")

(defonce app-state (atom {:text "Hello world!"
                          :active-panel :login
                          :user "test"}))

(defonce msg-list (atom []))
(defonce users (atom {}))
(defonce send-chan (async/chan))

;; Websocket Routines
(defn send-msg
  "Send a message over the websocket."
  [msg]
  (async/put! send-chan msg))

(defn send-msgs
  "Send multiple messages over the websocket."
  [svr-chan]
  (async/go-loop []
    (when-let [msg (async/<! send-chan)]
      (async/>! svr-chan msg)
      (recur))))

(defn notify
  "Send a notification with the provided message to the current user."
  [msg]
  (println "sending a notification..")
  (if (not= (.-permission (.-Notification js/window)) "granted")
    (.requestPermission (.-Notification js/window)))
  (println msg)
  ;; only send the notification if it was not sent by the current user
  (if (not= (:user msg) (:user @app-state))
    (let
     [notification (js/Notification.
                    (str "Smess (New message from " (:user msg) ")")
                    (clj->js {:icon "https://google.com" :body (:msg msg)}))]

      ;; (set! (.-onclick notification) (fn [] (.open js/window BASEURL)))
      )))

(defn receive-msgs
  "Receive messages from the websocket."
  [svr-chan]
  (async/go-loop []
    (if-let [new-msg (:message (<! svr-chan))]
      (do
        (case (:m-type new-msg)
          :init-users (reset! users (:msg new-msg))
          :chat ((fn []
                   (swap! msg-list conj (dissoc new-msg :m-type))
                   (notify new-msg)))
          :new-user (swap! users merge (:msg new-msg))
          :user-left (swap! users dissoc (:msg new-msg)))
        (recur))
      (println "Websocket closed"))))

(defn setup-websockets!
  "Connect websockets to one another."
  []
  (async/go
    (let [{:keys [ws-channel error]} (async/<! (ws-ch ws-url))]
      (if error
        (println (str "Received the websocket error " error))
        (do
          (send-msg {:m-type :new-user
                     :msg (:user @app-state)})
          (send-msgs ws-channel)
          (receive-msgs ws-channel))))))

;; View
(defn chat-input
  "Allow users to input text and submit it to send messages."
  []
  (let [v (atom nil)]
    (fn []
      [:div {:class "text-input"}
       [:form
        {:on-submit (fn [x]
                      (.preventDefault x)
                      (when-let [msg @v] (send-msg {:msg msg
                                                    :user (:user @app-state)
                                                    :m-type :chat}))
                      (reset! v nil))}
        [:div {:style {:display "flex"
                       :flex-direction "row"}}
         [:input {:type "text"
                  :value @v
                  :class "message-input"
                  :placeholder "Type a message to send to the chatroom"
                  :on-change #(reset! v (-> % .-target .-value))}]
         [:button {:type "submit"
                   :class "message-button"} "Send"]]]])))

(defn flip-group-chat-results [msglists]
  (reverse (map (fn [elem] {:user (:user elem)
                            :messages (reverse (:messages elem))}) msglists)))

;; input: (list {:msg, :user, :id})
;; return format:
;; list of
;; {:user: current user sending messages
;;  :messages: ({:user, :msg, :id})}
(defn group-chats [message-list]
  (flip-group-chat-results (:list (reduce
                                   (fn [last msg]
                                     (let
                                      [last-user (:user last)
                                       last-list (:list last)]
                                       (if (= (:user msg) last-user)
                       ;; if the current user is the same as the last:
                       ;; cons the current message onto the users data structure
                                         (let
                                          [cur-list-user (:user (first last-list))
                                           cur-list-msgs (:messages (first last-list))]
                                           {:user cur-list-user
                                            :list (cons
                                                   {:user cur-list-user
                                                    :messages (cons msg cur-list-msgs)}
                                                   (rest last-list))})
                       ;; otherwise, create a new data structure with the user and the message,
                       ;; carrying the last user's information with it
                                         (let [ret-obj {:user (:user msg)
                                                        :list (cons
                                                               {:user (:user msg) :messages (list msg)}
                                                               last-list)}]
                                           ret-obj))))
                 ;; start with an empty user and list
                                   {:user "" :list '()} message-list))))

(defn username-box [username]
  [:p {:class (str "username" (if (= (:user @app-state) username) " my-username" ""))}
   (if (= (:user @app-state) username)
     (str "me [ " username " ]")
     username)])

(defn chat-history []
  (reagent/create-class
   {:render (fn []
              [:div {:class "history"}
               (doall (for [usermsg (group-chats @msg-list)]
                        ^{:key (:user usermsg)}
                        [:div {:class "usermsg"}
                         (username-box (:user usermsg))
                         (for [m (:messages usermsg)]
                           ^{:key (:id m)} [:div {:key (:id m) :class "message"} (str (:msg m))])]))])

    :component-did-update (fn [this]
                            (let [node (reagent/dom-node this)]
                              (set! (.-scrollTop node) (.-scrollHeight node))))}))

(defn enable-notifications
  "Enable notifications for this browser."
  []
  (if (and (.-Notification js/window)
           (not= (.-permission (.-Notification js/window)) "granted"))
    (.requestPermission js/Notification)
    (println "This browser may not have notification capabilities.")))

(defn login-view
  "Allows users to pick a username and enter the chat."
  []
  (let [v (atom nil)]
    (fn []
      [:div {:class "login-container"}
       [:div {:class "login"}
        [:form
         {:on-submit (fn [x]
                       (.preventDefault x)
                       (swap! app-state assoc :user @v)
                       (swap! app-state assoc :active-panel :chat)
                       (setup-websockets!))}
         [:input {:type "text"
                  :class "username-input"
                  :value @v
                  :placeholder "Pick a username"
                  :on-change #(reset! v (-> % .-target .-value))}]
         [:br]
         [:button {:type "submit"
                   :onClick enable-notifications
                   :class "button-primary start-chatting-button"} "Start chatting"]]]])))

(defn sidebar
  "Shows all of the users currently in the channel."
  []
  [:div {:class "sidebar"}
   [:h5 "users"]
   (into [:ul]
         (for [[k v] @users]
           ^{:key k} (username-box v)))])

(defn chat-view
  "Displays all of the chat history."
  []
  [:div {:class "chat-container"}
   [chat-history]
   [chat-input]
   [:div {:class "header"}
    [:h3 "smess"]]
   [sidebar]])

(defn app-container
  "The entire front-end application with all of the different views."
  []
  (case (:active-panel @app-state)
    :login [login-view]
    :chat [chat-view]))

(reagent/render-component [app-container]
                          (. js/document (getElementById "app")))
