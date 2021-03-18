(ns smess.core
  (:require [reagent.core :as reagent :refer [atom]]
            [chord.client :refer [ws-ch]]
            [markdown.core :refer [md->html]]
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
  (if (not= (.-permission (.-Notification js/window)) "granted")
    (.requestPermission (.-Notification js/window)))
  ;; only send the notification if it was not sent by the current user
  (if (not= (:user msg) (:user @app-state))
    (js/Notification.
     (str "Smess: New message from " (:user msg))
     (clj->js {;; :icon "https://google.com" Add icon later once i have one
               :body (:msg msg)}))))

(defn receive-msgs
  "Receive messages from the websocket."
  [svr-chan]
  (async/go-loop []
    (if-let [new-msg (:message (<! svr-chan))]
      (do
        (case (:m-type new-msg)
          :init-users (reset! users (:msg new-msg))
          :chat (do
                  (swap! msg-list conj (dissoc new-msg :m-type))
                  (notify new-msg))
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
(defn markdown-preview
  "A window to preview chat input in markdown."
  [m]
  [:div {:class "markdown-preview"
         :dangerouslySetInnerHTML {:__html (md->html m)}}])

(defn chat-input
  "Allow users to input text and submit it to send messages."
  []
  (let [v (atom nil)]
    (fn []
      [:div {:class "text-input"}
       (if @v [:div {:class "markdown-preview-box"} (markdown-preview @v)] nil)
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

(defn to-clipboard [txt] (.writeText (.-clipboard js/navigator) txt))

(defn username-box
  "An interactive box containing the username."
  [username]
  [:p {:class (str "username" (if (= (:user @app-state) username) " my-username" ""))}
   (if (= (:user @app-state) username)
     (str "me [ " username " ]")
     username)])

(defn message
  "A single message."
  [m] [:div {:key (:id m) :id (:id m) :class "message"}
       (markdown-preview (:msg m))
       [:button {:id (str (:id m) "-text-button")
                 :class "text-button"
                 :onClick (fn [] (to-clipboard (:msg m)))}
        "copy text"]
       [:button "copy link"]
       [:button "reply"]])

(defn chat-history []
  (reagent/create-class
   {:render (fn []
              [:div {:class "history"}
               (doall (for [usermsg (group-chats @msg-list)]
                        ^{:key (:user usermsg)}
                        [:div {:class "usermsg"}
                         (username-box (:user usermsg))
                         (for [m (:messages usermsg)] (message m))]))])

    :component-did-update (fn [this]
                            (let [node (reagent/dom-node this)]
                              (set! (.-scrollTop node) (.-scrollHeight node))))}))

(defn enable-notifications
  "Enable notifications for this browser."
  []
  (if (.-Notification js/window)
    (if (not= (.-permission (.-Notification js/window)) "granted")
      (.requestPermission js/Notification))
    (.warn js/console "This browser may not have notification capabilities.")))

(defn ormap
  "Returns true if any of the values are true."
  [pred ls] (reduce (fn [acc i] (or acc (pred i))) nil ls))

(defn get-invalid-username-error
  "Gets the error associated with an invalid username if there is one."
  [val]
  (cond
    (or (= val "") (nil? val)) "Use a non-empty username."
    (ormap (partial = " ") (.split val "")) "The username should not include spaces."
    :else nil))

(defn login-view
  "Allows users to pick a username and enter the chat."
  []
  (let [v (atom nil)
        notif-error (atom nil)]
    (fn []
      [:div {:class "login-container"}
       [:form
        {:class "login"
         :on-submit (fn [x]
                      (.preventDefault x)
                       ;; if the user exists, they can enter the application.
                      (let
                       [username-error (get-invalid-username-error @v)]
                        (if (and @v (not username-error))
                          (do
                            (swap! app-state assoc :user @v)
                            (swap! app-state assoc :active-panel :chat)
                            (setup-websockets!))
                          (reset! notif-error username-error))))}
        [:input {:type "text"
                 :class "username-input"
                 :value @v
                 :placeholder "Pick a username"
                 :on-change #(let
                              [val (-> % .-target .-value)]
                               (reset! v val)
                               (reset! notif-error (get-invalid-username-error val)))}]
        [:button {:type "submit"
                  :onClick enable-notifications
                  :class "button-primary start-chatting-button"} "Start chatting"]]
       [:div {:class "error-tip-container"} (if @notif-error [:div {:class "error-tip"} @notif-error] nil)]])))

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
