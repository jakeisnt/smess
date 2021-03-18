(ns smess.db
  (:require [clojure.java.jdbc :refer
             [db-do-commands create-table-ddl query insert!]])
  (:gen-class))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(def testdata
  {:url "http://example.com",
   :title "SQLite Example",
   :body "Example using SQLite with Clojure"})

(def testchat
  {:name "Ideas!"
   :description "A chat for ideas."})

(def testmsg
  {:message "~~this is a crossed out passage.~~"
   :user "jake"})

(def testuser
  {:name "jake"})

(defn create-db
  "Create and instantiate the database with tables."
  []
  (try (db-do-commands db
                       (create-table-ddl :chats ;; all of the app's users
                                         [[:timestamp :datetime :default :current_timestamp] ;; when created
                                          [:archived :bool :default :false] ;; whether the chat is archived or not. initially false
                                          [:id "varchar(32)" :primary :key] ;; standard id. PK
                                          [:name :text] ;; name of the chat
                                          [:description :text] ;; *optional* description of the chat
                                          [:creator :text]]) ;; fk id of the person who made the chat
                       (create-table-ddl :users ;; all of teh app's users
                                         ;; all users in chats must be unique. see if there is a way to encode this.
                                         [[:timestamp :datetime :default :current_timestamp] ;; when created
                                          [:id "varchar(32)" :primary :key] ;; standard id
                                          [:name :text] ;; name of the user
                                          ])
                       (create-table-ddl :messages ;; all of the messages that are sent
                                         [[:timestamp :datetime :default :current_timestamp] ;; timestamp for message
                                          [:id :text] ;; unique id for message (created by websocket or database?)
                                          [:chat_id :foreign :key] ;; chat that the message is in
                                          [:message :text] ;; contents of the message. md compatible
                                          [:user :text] ;; fk to user who posted the message
                                          ]))
       (catch Exception e
         (println (.getMessage e)))))

(defn print-result-set
  "print the result set in tabular form"
  [result-set] (doseq [row result-set] (println row)))

(defn output
  "execute query and return lazy sequence"
  []
  (query db ["select * from news"]))

(defn test-db []
  (create-db)
  (insert! db :news testdata)
  (print-result-set (output)))
