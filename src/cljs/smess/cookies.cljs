(ns smess.cookies)

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
  []
  (let
   [cookiestr (.-cookie js/document)]
    (cookie->clj cookiestr)))

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
