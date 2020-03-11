(ns jjttjj.base.cmpnt.http-kit
  (:require
   [taoensso.timbre :as log]
   [taoensso.sente     :as sente]
   [clojure.string     :as str]
   [ring.middleware.defaults :as ring-dflt]
   [ring.middleware.anti-forgery :as anti-forgery]
   [compojure.core     :as comp :refer (defroutes GET POST)]
   [compojure.route    :as route]
   [hiccup.core        :as hiccup]
   [org.httpkit.server :as http-kit]
   [medley.core :as m]
   [objection.core :as obj]))


(defn landing-pg-handler [ring-req]
  (hiccup/html
   "<!DOCTYPE html>\n"
   [:html
    [:head
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
    [:body
     (let [csrf-token
           ;; (:anti-forgery-token ring-req) ; Also an option
           (force anti-forgery/*anti-forgery-token*)]
       [:div#sente-csrf-token {:data-csrf-token csrf-token}])
     [:div#app]
     [:script {:src "js/main.js"
               ;; or for :app "js/main.js"
               }]]]))

(defn dflt-routes []
  (comp/routes
   (GET  "/"      ring-req (landing-pg-handler ring-req))
   (route/resources "/public") ;;Static files, notably public/main.js
   (route/not-found "<h1>Page not found</h1>")))

(defn wrap-defaults [routes]
  (ring-dflt/wrap-defaults routes ring-dflt/site-defaults))

(defn http-server [handler & [opt]]
  (let [stopfn (http-kit/run-server handler
                                    {:port (get opt :port 0)})
        port   (-> stopfn meta :local-port)
        uri    (format "http://localhost:%s/" port)]
    (when (:browse! opt)
      (try
        (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
        (catch java.awt.HeadlessException _)))
    stopfn))


