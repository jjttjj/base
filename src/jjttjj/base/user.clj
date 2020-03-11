(ns jjttjj.base.user
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [semantic-csv.core :as sc])
  (:import (java.nio.file Files Paths)))

"COVID-19/csse_covid_19_data/csse_covid_19_time_series"

(def data-root-str "COVID-19/")


(->> (file-seq (io/file data-root-str))
     (filter #(re-find #"\.csv" (.getName %)))
     (map (fn [f]
            [(.getCanonicalPath  f)
             ]))
     ;;(map #(.getName %))
     
     ;;(filter #(.))
     )

(->> (csv/read-csv
      (io/reader
       "COVID-19/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv  "))
     ;;first
     ;;sc/mappify
     )

;;time_series_19-covid-Recovered.csv
;;time_series_19-covid-Confirmed.csv

;;rename to server api?
;;have these be maps of state...

(def current-dir (atom (System/getProperty "user.dir")))

(def current-file (atom nil))

(defn dirs+files []
  (let [{dirs true files false}
        (->> @current-dir
             io/file
             .listFiles
             (group-by #(.isDirectory %)))]
    [(map str dirs)
     (map str files)]))


;;(defn set-dir [new-path] (reset! current-dir new-path))
#_
(->> (io/file @current-dir)
     .listFiles
     ;;((juxt filter remove) #(.isDirectory %))
     (group-by #(.isDirectory %))
     )


