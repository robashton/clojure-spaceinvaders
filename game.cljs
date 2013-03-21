(ns game)

(defn context []
  (let [target (.getElementById js/document "target")]
    [
      (.getContext target "2d") 
      (. target -width)
      (. target -height)
    ]
  )
)

(defn clearScreen [[ctx width height]]
  (set! (. ctx -fillStyle) "#FFF")
  (.clearRect ctx 0 0 width height) 
)

(defn drawSquare [[ctx width height] x y w h]
  (set! (. ctx -fillStyle) "#FF0")
  (.fillRect ctx x y w h) 
)

(defn initState []
 (for [x (range 0 100 10)
       y (range 0 100 10)]
   [x y]
 )
)

(defn tick [enemies]
  (let [ctx (context)] 
    (clearScreen ctx) 
    (doseq [[x y] enemies] (drawSquare ctx x y 5 5))
    (js/setTimeout (fn []
      (tick enemies) 
    ) 33  )
  )
)

(defn ^:export init []
  (tick (initState)) 
)
