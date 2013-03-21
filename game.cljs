(ns game)

(defn context [width height]
  (let [target (.getElementById js/document "target")]
    [
      (.getContext target "2d") 
      (set! (. target -width) width)
      (set! (. target -height) height)
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
 [
   1
   (for [x (range 0 16 2)
         y (range 0 8 2)]
     [(* x 30) (* y 30) 20 20]
   )
 ]
)

(defn getNextDirection [current enemies]
  (if (= current 1)
    (let [right (apply max (map (fn [[x y w h] e] x) enemies))]
      (if(> right 600) -1 1)
    )
    (let [left (apply min (map (fn [[x y w h] e] x) enemies))]
      (if(< left 0) 1 -1)
    )
  )
)

(defn doLogic [[direction enemies]]
  [
    (getNextDirection direction enemies)
    (for [[x y w h] enemies]
      (if(= direction 1)
        [(inc x) y w h]
        [(dec x) y w h]
      )
    )
  ]
)

(defn tick [ctx state]
  (let [[dir enemies] state]
    (clearScreen ctx) 
    (doseq [[x y w h] enemies] (drawSquare ctx x y w h))
    (js/setTimeout (fn []
      (tick ctx (doLogic state))
    ) 33  )
  )
)

(defn ^:export init []
  (let [ctx (context 640 480)] 
    (tick ctx (initState)) 
  )
)
