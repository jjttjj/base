(ns cljsjs.markdown
  (:require ["markdown" :as md] ))

;;(js/goog.object.set md "" crc)
(js/goog.exportSymbol "markdown" md)

