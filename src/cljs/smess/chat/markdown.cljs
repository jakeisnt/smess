(ns smess.chat.markdown
  (:require
   [rum.core :as rum]
   [markdown.core :refer [md->html]]))

(rum/defc markdown-preview
  "A window to preview chat input in markdown."
  [txt]
  [:div {:class "markdown-preview"
         :dangerouslySetInnerHTML {:__html (md->html txt)}}])
