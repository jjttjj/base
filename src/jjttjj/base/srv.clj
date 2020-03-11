(ns jjttjj.base.srv
  (:require
   [taoensso.timbre :as log]
   [clojure.string     :as str]
   [clojure.core.async :as a]
   [medley.core :as m]
   [objection.core :as obj]
   [taoensso.timbre.tools.logging :refer [use-timbre]]
   [jjttjj.base.sente :as ws]
   [jjttjj.base.http :as http]
   [jjttjj.base.user]
   [sci.core :as sci]))

(use-timbre)

#_(defmethod ws/sente-handler :clj/eval [{:keys [?reply-fn ?data uid]}]
  (?reply-fn
   (binding [*ns* (the-ns 'jjttjj.base.user)]
     (eval (read-string (:code-string ?data))))))

(defmethod ws/sente-handler :sci/eval [{:keys [?reply-fn ?data uid]}]
  (?reply-fn
   (sci/eval-string (:code-string ?data)
                    {:bindings {'println println}})))

(defn start []
  (let [{:keys [send-fn ch-recv] :as sente} (obj/register (ws/new))
        http   (obj/construct
                {:stopfn (fn [this] (this :timeout 100))
                 :deps   [sente]
                 :name   (str  "http-kit server")}
                (http/http-server
                 sente
                 {:port (Integer. (or (System/getenv "PORT") 5000))}))
        router (obj/construct
                {:stop-fn (fn [this] (this))
                 :deps    [sente]}
                (ws/start-router sente))]
    
    {::sente        sente
     ::sente-router router
     ::http         http}))
