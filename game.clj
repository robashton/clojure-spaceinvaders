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
  (set! (. ctx -fillStyle) "#000")
  (.fillRect ctx x y w h) 
)


(defn tick [x]
  (let [ctx (context)] 
    (clearScreen ctx) 
    (drawSquare ctx x 0 100 100)  
    (if (<= x 1000) 
      (js/setTimeout (fn []
        (tick (inc x)) 
      ) 33  )
    )
  )
)



(defn ^:export init []
  (tick 0) 
)
