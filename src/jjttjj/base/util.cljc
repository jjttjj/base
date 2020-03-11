(ns jjttjj.base.util
  (:require [backtick :as backtick]))

#?(:clj (backtick/defquote code identity))

#?(:cljs
   (defn clog [& xs] (apply js/console.log xs) (last xs)))

#?(:cljs
   (defn nan? [x] (js/Number.isNaN x)))

;;https://gist.github.com/SegFaultAX/3607101
(def levenshtein
  (memoize 
   (fn lev [s1 s2]
     (cond
       (zero? (count s1))        (count s2)
       (zero? (count s2))        (count s1)
       (= (first s1) (first s2)) (lev (rest s1) (rest s2))
       :else                     (inc (min (lev (rest s1) s2)
                                           (lev s1 (rest s2))
                                           (lev (rest s1) (rest s2))))))))
