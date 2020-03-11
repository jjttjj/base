(ns jjttjj.base.cmpnt.sente
  (:require [taoensso.timbre :as log]
            [taoensso.sente :as sente]
            [cognitect.transit :as transit]
            [taoensso.sente.packers.transit :as sente-transit]
            [objection.core :as obj]
            #?@(:clj [[taoensso.sente.server-adapters.http-kit :as sente-adapter]
                      [compojure.core :as comp :refer (defroutes GET POST)]
                      [compojure.route :as route]]))
  #?(:cljs (:require-macros [jjttjj.base.cmpnt.sente :refer [on-open]])))

(def packer
  (sente-transit/->TransitPacker
   :json
   (merge ;;{:handlers {}}
    {:transform transit/write-meta})
   {}))

#?(:clj
   (defn routes [sente]
     (comp/routes
      (GET  "/chsk"  ring-req ((:ajax-get-or-ws-handshake-fn sente) ring-req))
      (POST "/chsk"  ring-req ((:ajax-post-fn sente)                ring-req)))))

(defn mk [& [opt]]
  #?(:clj
     (sente/make-channel-socket-server!
      (sente-adapter/get-sch-adapter)
      (merge opt {:packer packer}))
     
     :cljs
     (sente/make-channel-socket-client!
      "/chsk" ; Must match server Ring routing URL
      (or
       (:csrf-token opt)
       (if-let [el (.getElementById js/document "sente-csrf-token")]
         (.getAttribute el "data-csrf-token")
         (assert false "cannot find csrf element")))
      {:type           :auto
       :packer         packer
       :wrap-recv-evs? false})))

(defmulti sente-handler :id)

(defn wrapped-sente-handler
  [{:as ev-msg :keys [id ?data event]}]
  (sente-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (sente-handler ev-msg)) ; Handle event-msgs on a thread pool
  )


(defmethod sente-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (log/debug "unhandled msg, id:" id ?data)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

(defn basic-router [{:keys [ch-recv] :as sente}]
  (sente/start-chsk-router! ch-recv wrapped-sente-handler))

;;for cljs 
(defmacro on-open [sente & body]
  `(if (:open? @(:state ~sente))
     (do ~@body)
     (add-watch (:state ~sente)
                (name (gensym))
                (fn [k# r# _# state#]
                  (when (:open? state#)
                    ~@body
                    (remove-watch r# k#))))))


#_(defn cmpnt []
    (obj/register (mk)))
