(ns game)

(def keyStates (atom {}))

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

(defn drawSquare [[ctx width height] x y w h c]
  (set! (. ctx -fillStyle) c)
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

(defn initPlayer [x y w h]
 {
  :x x
  :y y
  :w w
  :h h
 }
)

(defn initBullet [x y w h]
 {
  :x x
  :y y
  :w w
  :h h
 }
)

(defn initBullets []
  {
    :lastFiringTicks 0
    :active ()
  }
)

(defn initState []
 { 
   :direction 1
   :enemies (for [x (range 0 16 2)
                  y (range 0 8 2)]
              (initEnemy x y 20 20)
   )
   :player (initPlayer 200 430 20 20)
   :bullets (initBullets)
 } 
)

(defn directionLogic [state]
  (let [{:keys [direction enemies]} state]
    (if (= direction 1)
      (let [right (apply max (map :x enemies))]
        (if(> right 600) (assoc state :direction -1) state)
      )
      (let [left (apply min (map :x enemies))]
        (if(< left 0) (assoc state :direction 1) state)
      )
    )
  )
)

(defn enemiesLogic [state]
  (let [direction (:direction state)
        enemies (:enemies state)
        func (if(= direction 1) inc dec)
       ]
    (assoc state :enemies
      (for [enemy enemies]
        (assoc enemy :x (func (:x enemy)))
      )
    )
  )
)

(defn bulletsLogic [state]
  (tryAndFire
    (moveBullets state)
  )
)

(defn moveBullets [state]
  (let [bullets (:bullets state)
        active (:active bullets)]
    (assoc state :bullets 
      (assoc bullets :active
        (for [bullet active]
          (assoc bullet :y (dec (:y bullet)))
        )
      )
    )
  )
)

(defn fire [state]
  (let [bullets (:bullets state)
        active (:active bullets)
        player (:player state)]
    (assoc state :bullets 
      (assoc bullets :active
        (cons 
          (initBullet (:x player) (:y player) 5 5)
          active
        )
       )
     )
  )
)

(defn tryAndFire [state]
  (if (@keyStates 32)
    (fire state)
    state
  )
)

(defn applyMod [m k func]
  (assoc m k (func (m k)))
)

(defn playerLogic [state]
  (let [player (:player state)  
        left (@keyStates 37)
        right (@keyStates 39)
       ]
    (assoc state :player 
      (cond (= left true) (applyMod player :x dec)
            (= right true) (applyMod player :x inc)
            :else player
      )
    )
  )
)


(defn enemiesRender [ctx state]
  (let [enemies (:enemies state)]
    (doseq [enemy enemies] 
      (let [{:keys [x y w h]} enemy]
        (drawSquare ctx x y w h "#FF0")
      )
    )
  )
)

(defn bulletsRender [ctx state]
  (doseq [bullet (:active (:bullets state))] 
    (let [{:keys [x y w h]} bullet]
      (drawSquare ctx x y w h "#000")
    )
  )
)

(defn playerRender [ctx state]
  (let [player (:player state)]
    (let [{:keys [x y w h]} player]
      (drawSquare ctx x y w h "#F00")
    )
  )
)

(defn doLogic [state]
  (bulletsLogic
    (playerLogic
      (enemiesLogic
        (directionLogic state)
      )
    )
  )
)

(defn renderScene [ctx state]
  (enemiesRender ctx state)
  (playerRender ctx state)
  (bulletsRender ctx state)
)

(defn tick [ctx state]
  (clearScreen ctx) 
  (renderScene ctx state)
  (js/setTimeout (fn []
    (tick ctx (doLogic state))
  ) 33  )
)

(defn ^:export init []
  (hookInputEvents)
  (let [ctx (context 640 480)] 
    (tick ctx (initState)) 
  )
)

(defn hookInputEvents []
  (.addEventListener js/document "keydown" 
   (fn [e]
    (setKeyState (. e -keyCode) true)
     false
   )
  )
  (.addEventListener js/document "keyup" 
   (fn [e]
    (setKeyState (. e -keyCode) false)
     false
   )
  )
)

(defn setKeyState [code, value]
  (swap! keyStates assoc code value)
)
