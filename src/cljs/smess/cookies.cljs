(ns smess.cookies)

(defn cookie->clj
  "Gets the browser cookie as a clojure map into memory."
  []
  (let
   [cookie (.-cookie js/document)]
    ;; creates {:key value :key2 value2}
    (apply merge
           ;; creates ({:key value} {:key value})
           (map #(hash-map (keyword (first %1)) (str (second %1)))
         ;; generates ((key, value), (key, value)) etc...
                (map
                 (fn [str] (.split str "="))
                 (.split cookie ";"))))))

(defn clj->cookie
  "Convert a clojure map to a cookie"
  [obj]
  (let
   [keys (keys obj)
    lst (map (fn [key] (str key "=" (get obj key))) keys)
    cookiestr (.join lst ";")]
    cookiestr))

(defn clj->cookie!
  "Convert a clojure map to a cookie, writing the cookie."
  [obj]
  (set! (.-cookie js/document) (clj->cookie obj)))
