(ns smess.utils)

(defn ormap
  "Returns true if any of the values are true."
  [pred ls] (reduce (fn [acc i] (or acc (pred i))) nil ls))

(defn focus-element
  "Focus an HTML element with the provided ID."
  [elem-id]
  (let
   [elem (.getElementById js/document elem-id)]
    (.select elem)
    (.focus elem)))

(defn to-clipboard
  "Copy a line of text to the clipboard."
  [txt] (.writeText (.-clipboard js/navigator) txt))

(defn scroll-to-top
  "Scroll a specific DOM element to the top of the page."
  [elem]
  (set! (.-scrollTop node) (.-scrollHeight node)))
