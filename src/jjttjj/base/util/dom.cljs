(ns jjttjj.base.util.dom)


(defn $$
  ([query] ($$ query js/document))
  ([query elem] (.querySelectorAll elem (name query))))

(defn $
  ([query]  ($ query js/document))
  ([query elem] (.querySelector elem (name query))))
