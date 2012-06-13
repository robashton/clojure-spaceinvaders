(ns game)

(defn drawRect [x y w h]
  (let [target (.getElementById js/document "target")
        context (.getContext target "2d")]
    (.fillRect context x y w h)    
  )
)

(defn tick [x]
  (drawRect x 0 100 100)
  (if (<= x 1000) 
    (js/setTimeout (fn []
      (tick (inc x)) 
    ) 33  )
  )
)


(defn ^:export init []
  (tick 0) 
)
