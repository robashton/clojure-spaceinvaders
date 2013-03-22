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

(defn initEnemy [x y w h]
 {
  :x (* x 30)
  :y (* y 30)
  :w w
  :h h
 }
)

(defn initState []
 { 
   :direction 1
   :enemies (for [x (range 0 16 2)
                  y (range 0 8 2)]
              (initEnemy x y 20 20)
   )
 } 
)

(defn directionLogic [state]
  (let [{:keys [direction enemies]} state]
    (if (= direction 1)
      (let [right (apply max (map :x enemies))]
        (if(> right 600) -1 1)
      )
      (let [left (apply min (map :x enemies))]
        (if(< left 0) 1 -1)
      )
    )
  )
)

(defn enemiesLogic [state]
  (let [{:keys [direction enemies]} state
        func (if(= direction 1) inc dec)
       ]
    (for [enemy enemies]
      {
        :x (func (:x enemy))
        :y (:y enemy)
        :w (:w enemy)
        :h (:h enemy)
      }
    )
  )
)

(defn doLogic [state]
  {
    :direction (directionLogic state)
    :enemies (enemiesLogic state)
  }
)

(defn tick [ctx state]
  (let [enemies (:enemies state)]
    (clearScreen ctx) 
    (doseq [enemy enemies] 
      (let [{:keys [x y w h]} enemy]
        (drawSquare ctx x y w h)
      )
    )
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
