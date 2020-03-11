(ns cljsjs.snabbdom
  (:require ["snabbdom" :as snabbdom]
            ["snabbdom/modules/eventlisteners" :default el]
            ["snabbdom/modules/attributes" :default attr]
            ["snabbdom/modules/props" :default props]
            ["snabbdom/modules/style" :default style]))



(js/goog.object.set snabbdom "eventlisteners" el)
(js/goog.object.set snabbdom "attributes" attr)
(js/goog.object.set snabbdom "props" props)
(js/goog.object.set snabbdom "style" style)
(js/goog.exportSymbol "snabbdom" snabbdom)

