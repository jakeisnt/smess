(ns smess.db
  (:require
    [datascript.core :as d]))

(def schema {;; a user of the system
             :user/name {} ;; name of the user (string)
             ;; TODO is id needed?
             :user/me {}   ;; is the user me?
             :user/state {} ;; has the user loaded etc?

             ;; a single chat message
             :message/text {} ;; the message itself
             :message/reply-to {:db.valueType :db.type/ref} ;; ref
             :message/author {:db/valueType :db.type/ref} ;; creator of the message
             :message/type {} ;; type of message (enum?)

             ;; all of the messages sent in a chat room
             :room/messages {:db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

;; get the user by specified id
(defn get-user-by-id [db id]
  (-> (d/entity db id)
      (select-keys [:db/id :user/name :user/me :user/state])))


;; get a user by id
(defn get-user [id]
  (user-by-id @conn id))

(defn get-messages []

  )

;; create user when we have userid
(defn user-stub [uid]
  {:db/id uid
   :user/name "Loading..."
   :user/state :loading })

; (defn load-user [uid]
;   (d/transact! conn))
