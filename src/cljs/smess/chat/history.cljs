(ns smess.chat.history
  (:require
    [rum.core :as rum]
    [smess.utils :refer [to-clipboard focus-element]]
    [smess.chat.username :refer [username-box]]
    [smess.chat.reply :refer [message-reply]]
    [smess.chat.input :refer [input-box-name]]
    [smess.chat.markdown :refer [markdown-preview]]))

(defn- flip-group-chat-results
  "Flip the results of 'group-chats' to display chat messages from first sent to most recent."
  [msglists]
  (reverse (map (fn [elem] {:user (:user elem)
                            :id (:id elem)
                            :messages (reverse (:messages elem))}) msglists)))

;; input: (list {:msg, :user, :id})
;; return format:
;; list of
;; {:user: current user sending messages
;;  :id: some unique id for the block. can be redundant with messages
;;  :messages: ({:user, :msg, :id})}
(defn- group-chats
  "Reformat chat messages into a better renderable format."
  [message-list]
  (flip-group-chat-results
    (:list (reduce
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
                                          {:user (:user msg)
                                           :id (:id msg)
                                           :key (:id msg)
                                           :messages (list msg)}
                                          last-list)}]
                     ret-obj))))
             ;; start with an empty user and list
             {:user "" :list '()} message-list))))


(rum/defc message
  "A single message."
  [m selected-message]
  [:.message {:id (str "msg-" (:id m))
              :style {:background-color (and (= (:id m) (:id @selected-message)) "azure")}}
   (and (:reply-to m) [:.message-reply-box (message-reply (:reply-to m))])
   [:.message-content (markdown-preview (:msg m))
    [:.message-buttons
     [:button.text-button {:on-click #((to-clipboard (:msg m)))} "copy text"]
     [:button "copy link"]
     [:button {:on-click #((if (= @selected-message m)
                             (reset! selected-message nil)
                             (reset! selected-message m))
                           (focus-element input-box-name))}
      "reply"]]]])


(rum/defc chat-history < rum/reactive
  "Display the history of the chat."
  [msg-list app-state selected-message]
  [:.history
   (doall (for [usermsg (group-chats (rum/react msg-list))]
            [:.usermsg {:key (str (:user usermsg) "-" (:id usermsg))}
             (username-box (:user usermsg) app-state)
             (for [m (:messages usermsg)]
               (rum/with-key (message m selected-message) (str "msg-" (:id m))))]))])
