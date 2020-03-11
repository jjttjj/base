 (ns jjttjj.base.client
   (:require

    #_[diy.hoplon :as h
              :refer [div p a button ul i img li input select option form span
                     label h1 h2 h3 h4 h5
                     table td tr th]]
    ;;[diy.core :refer [defelem]]
    ;;[diy.tpl :as tpl :refer [for-tpl if-tpl when-tpl case-tpl]]
    [hoplon.core :as h :refer-macros [defelem]
     :refer [i div]]
            [goog.style :as gsty]
            [goog.dom.forms :as gdomf]
            [goog.functions :as gfn]
            [goog.positioning :as gpos]
            [goog.events :as gev]
            [goog.dom :as gdom]
            [goog.object :as gobj]
            [goog.array :as garr]
            [goog.events.EventType :as evt]
            [goog.events.KeyCodes :as kc]
            [goog.dom.classlist :as gclass]
            [goog.net.XhrIo :as xhr]
            [goog.date.Date :as gdt]
            [tick.alpha.api :as t]
            tick.timezone
            [taoensso.timbre :as log]
            [javelin.core :as j :refer [cell cell= defc defc=]]
            [taoensso.sente :as sente]
            [jjttjj.base.sente :as ws :refer [on-open]]


            ["codemirror" :as CodeMirror]
            ["codemirror/mode/clojure/clojure" :as clj]
            ["codemirror/addon/edit/matchbrackets" :as match-brackets]
            ["codemirror/addon/edit/closebrackets" :as show-brackets]
            ["codemirror/addon/hint/show-hint" :as hint ]
            ["codemirror/keymap/emacs" :as emacs]

            ["parinfer-codemirror" :as par-cm]
            [sci.core :as sci]
            

            [jjttjj.base.exp.text-editor :as ed]
            [hoplon.ui         :refer [window pane fore line lines file files path line-path image video markdown b= t=
                                       bind-in!]]
            [hoplon.ui.elems  :refer [box doc out mid in elem?]]
            [hoplon.ui.attrs   :refer [r]]
            [cljsjs.markdown]
            ;;[backtick :refer [template]]
            [zprint.core :as zp]
            )
  (:require-macros [jjttjj.base.macros :refer [code]]))

(enable-console-print!)

(defn code-str [form & [opt]]
  (zp/zprint-str form #_width (merge {:style :justified} opt)))

(defonce csrf-token (.getAttribute (.getElementById js/document "sente-csrf-token")
                                   "data-csrf-token"))

(def SENTE (ws/new {:csrf-token csrf-token}))

(def default-timeout 10000)

;;should this be "on-open" or force user to wrap in one.
;;the latter is weird because this has an implicit dep on sente
(defn send!
  ([event cb] (send! event default-timeout cb))
  ([event timeout cb]
   ((:send-fn SENTE)
    event
    timeout
    (fn [x]
      (if (sente/cb-success? x)
        (cb x)
        (throw (js/Error. (pr-str "Callback failed:" [event x]))))))))

(defn |=
  ([code cb] (|= code default-timeout cb))
  ([code timeout cb]
   (on-open SENTE
     (send! [:sci/eval {:code-string
                        (if (string? code) code (pr-str code))}]
            timeout cb))))


(|= "(println \"hi\")"  prn)
(|= (code (println "bye"))  prn)

(def black  0x1F1F1FFF)

(defn my-windowable [ctor]
  (fn [{:keys [language scroll] :as attr} kids]
    (doto (ctor attr kids)
      (bind-in! [out .-lang] (or language "en"))
      (bind-in! [out .-style .-width]     "100%")
      (bind-in! [out .-style .-height]    "100%")
      (bind-in! [mid .-style .-width]     "100%")
      (bind-in! [mid .-style .-height]    (cell= (when-not scroll "100%"))) ; added
      (bind-in! [mid .-style .-margin]    "0")
      (bind-in! [mid .-style .-fontSize]  "100%")
      (bind-in! [out .-style .-overflow] (cell= (when-not scroll "hidden"))))))

#_(defn my-window2 [ctor]
    (let [scroll nil]
      
      (doto (box (fn [attr kids]
                   ))
        (bind-in! [out .-lang] (or #_language "en"))
        (bind-in! [out .-style .-width]     "100%")
        (bind-in! [out .-style .-height]    "100%")
        (bind-in! [mid .-style .-width]     "100%")
        (bind-in! [mid .-style .-height]    (cell= (when-not scroll "100%"))) ; added
        (bind-in! [mid .-style .-margin]    "0")
        (bind-in! [mid .-style .-fontSize]  "100%")
        (bind-in! [out .-style .-overflow] (cell= (when-not scroll "hidden"))))))

;;(def my-window (-> h/div box h/node my-window h/parse-args))

;;(defmethod h/do! :value [elem _ v] (gdomf/setValue elem v))

(defmethod h/do! :height [elem k v] (gsty/setHeight elem v))
(defmethod h/do! :width [elem k v] (gsty/setWidth elem v))

(defn clog [& xs] (apply js/console.log xs) (last xs))


(def init-str
  (code-str
   (code
    (pane :s 1 :sc black :m 10 :g 10
          (pane :sc black :s 1 "hi")
          (pane :sc black :s 1 :eh (r 1 2) :ev 200
                :a :mid "bye")
          (for [x (range 19)]
            (pane :sc black :s 2 :e 40 :a :mid
                  x))))
   {:fn-map {"pane" :pair-fn
             ;;:arg1-pair
             }}))
;;(pr-str (code ))
;;todo: parse multiple.
(defn ui []
  (let [editor (ed/dflt-editor)
        txt    (cell
                init-str
                #_""
                #_(pr-str (code (pane :s 1 :sc black :m 10 :g 10
                                      (pane :sc black :s 1 "hi")
                                      (pane :sc black :s 1 :eh (r 1 2)"bye")
                                      (for [x (range 19)]
                                        (pane :sc black :s 2 x :eh 40 :a :mid)))))
                )
        evaled (cell= (sci/eval-string txt
                                       {:bindings {'div   div
                                                   'pane  pane
                                                   'black black
                                                   'r     r}}))]
    (cell= (log/spy txt))
    (.setValue editor @txt)
    ;;    (.refresh editor)
    (js/setTimeout  (fn []
                      (.refresh editor)
                      (.focus editor)) 300)        
    (.on  editor "change" (fn [cm change]
                            (reset! txt (.getValue cm))))
    
    
    (pane :e (r 1 1) :s 2 :sc black
          (pane :s 1 :sc black :eh (r 1 2) :ev (r 1 1)
                (ed/->elem editor))
          (pane :s 1 :sc black :eh (r 1 2) :ev (r 1 1)
                evaled))))

#_(pane :s 1 :sc black :m 10 :g 10
        (pane :sc black :s 1 "hi")
        (pane :sc black :s 1 "bye"))

#_(pane :s 1 :sc black :m 10 :g 10
      (pane :sc black :s 1 "hi")
      (pane :sc black :s 1 "bye")
      (for [x (range 19)]
        (pane :sc black :s 2 x :eh 20)))



(def stylesheets
  [;;"/css/tailwind.min.css"
   "/css/datepicker.css"
   "/css/codemirror.css"
   "/css/cm-themes/zenburn.css"
   "/css/show-hint.css"
   "https://fonts.googleapis.com/icon?family=Material+Icons"])


(defn append [node & children]
  (doto node (gdom/append (garr/flatten (clj->js children)))))

(defn $$
  ([query] ($$ query js/document))
  ([query elem] (.querySelectorAll elem (name query))))

(defn $
  ([query]  ($ query js/document))
  ([query elem] (.querySelector elem (name query))))

(defn ^:dev/after-load start []
  ;;reset
  ;;(set! (.-innerHTML ($ "#app")) "")
  ;;(set! (.-innerHTML ($ "head")) "")
  
  #_(append js/document.head
          (for [href stylesheets]
            (h/link {:href href :rel "stylesheet"})))

  ;;(($ "#app") :width "100%" :height "100%")
  ;;(append ($ "#app") (ui))
  (window :styles stylesheets
          (ui)
          )
  )

(start)
  
#_(window
 (pane "hi there123"))
