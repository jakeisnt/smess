(ns smess.cookies)

(defn get-cookie!
  "Get the document's cookie."
  [] (.-cookie js/document))

(defn cookie->clj
  "Convert a cookie string to a clojure map."
  [cookiestr]
    ;; creates {:key value :key2 value2}
  (apply merge
           ;; creates ({:key value} {:key value})
         (map #(hash-map (keyword (first %1)) (str (second %1)))
         ;; generates ((key, value), (key, value)) etc...
              (map
               (fn [str] (.split str "="))
               (.split cookiestr ";")))))

(defn cookie->clj!
  "Gets the browser cookie as a clojure map into memory."
  [] (cookie->clj (get-cookie!)))

(defn set-cookie!
  "Set the cookie value to a specific string."
  [cookiestr]
  (set! (.-cookie js/document) cookiestr))

(defn clj->cookie
  "Convert a clojure map to a cookie"
  [obj]
  (.join
   (map (fn [key] (str key "=" (get obj key))) (keys obj))
   ";"))

(defn clj->cookie!
  "Convert a clojure map to a cookie, writing the cookie."
  [obj]
  (set-cookie! (clj->cookie obj)))
