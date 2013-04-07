(ns game)

(def key-states (atom {}))

(defn context [width height]
  (let [target (.getElementById js/document "target")]
    [(.getContext target "2d") 
      (set! (. target -width) width)
      (set! (. target -height) height)]))

(defn clear-screen [[ctx width height]]
  (set! (. ctx -fillStyle) "#FFF")
  (.clearRect ctx 0 0 width height))

(defn render-rect [[ctx width height] rect c]
  (set! (. ctx -fillStyle) c)
  (let [{:keys [x y w h]} rect]
    (.fillRect ctx x y w h)))

(defn create-rect [x y w h]
 { :x x
   :y y
   :w w
   :h h })

(defn create-bullets []
  { :lastFiringTicks 0
    :active () })

(defn create-state []
 { :direction 1
   :enemies (for [x (range 0 480 60)
                  y (range 0 240 60)]
              (create-rect x y 20 20))
   :player (create-rect 200 430 20 20)
   :bullets (create-bullets) })

(defn update-direction [state]
  (let [{:keys [direction enemies]} state]
    (if (= direction 1)
      (let [right (apply max (map :x enemies))]
        (if(> right 600) (assoc state :direction -1) state))
      (let [left (apply min (map :x enemies))]
        (if(< left 0) (assoc state :direction 1) state)))))

(defn update-enemies [state]
  (let [direction (:direction state)
        enemies (:enemies state)
        func (if(= direction 1) inc dec)
       ]
    (assoc state :enemies
      (for [enemy enemies]
        (update-in enemy [:x] func)))))

(defn update-firing-ticks [state]
  (let [bullets (:bullets state)
        ticks (:lastFiringTicks bullets)]
    (if (= ticks 0) 
      state
      (if (= (rem ticks 30) 0)
        (assoc-in state [:bullets :lastFiringTicks] 0)
        (update-in state [:bullets :lastFiringTicks] inc)))))

(defn update-bullets [state]
  (try-and-fire
    (update-firing-ticks
      (collide-bullets
        (move-bullets state)))))

(defn move-bullets [state]
  (let [bullets (:bullets state)
        active (:active bullets)]
    (assoc-in state [:bullets :active]
      (for [bullet active]
        (update-in bullet [:y] dec)))))

(defn increment-firing-ticks [state]
  (assoc-in state [:bullets :lastFiringTicks] 1)
)

(defn add-bullet-in-player-location [state]
  (let [player (:player state)]
    (assoc-in state [:bullets :active]
      (cons 
        (create-rect (:x player) (:y player) 5 5)
        (get-in state [:bullets :active])))))

(defn collides-with [one two]
  (let [one-left (:x one)
        one-right (+ (:x one) (:w one))
        one-top (:y one)
        one-bottom (+ (:y one) (:h one))
        two-left (:x two)
        two-right (+ (:x two) (:w two))
        two-top (:y two)
        two-bottom (+ (:y two) (:h two))]
    (cond (< one-right two-left) false
          (> one-left two-right) false
          (< one-bottom two-top) false
          (> one-top two-bottom) false
          :else true)))

(defn collide-bullets [state]
  (assoc 
    (assoc-in state [:bullets :active]
      (remove #(collides-with-any % (:enemies state)) (active-bullets state)))
    :enemies
      (remove #(collides-with-any % (active-bullets state)) (:enemies state))))

(defn collides-with-any [one, others]
  (some #(collides-with % one) others))

(defn active-bullets [state] (get-in state [:bullets :active]))

(defn fire [state]
  (increment-firing-ticks
    (add-bullet-in-player-location state)))

(defn can-fire [state]
  (= (get-in state [:bullets :lastFiringTicks]) 0))

(defn try-and-fire [state]
  (if (and (@key-states 32) (can-fire state))
    (fire state) state))

(defn update-player [state]
  (let [left (@key-states 37)
        right (@key-states 39)]
    (cond (= left true) (update-in state [:player :x] dec)
          (= right true) (update-in state [:player :x] inc)
          :else state)))

(defn render-rects [ctx rects colour]
  (doseq [rect rects] 
    (render-rect ctx rect colour)))

(defn render-enemies [ctx state]
  (render-rects ctx (:enemies state) "#FF0"))

(defn render-bullets [ctx state]
  (render-rects ctx (get-in state [:bullets :active]) "#000"))

(defn render-player [ctx state]
  (render-rect ctx (:player state) "#F00"))

(defn update-state [state]
  (update-bullets
    (update-player
      (update-enemies
        (update-direction state)))))

(defn render-scene [ctx state]
  (render-enemies ctx state)
  (render-player ctx state)
  (render-bullets ctx state))

(defn tick [ctx state]
  (clear-screen ctx) 
  (render-scene ctx state)
  (js/setTimeout (fn []
    (tick ctx (update-state state))) 33))

(defn ^:export init []
  (hook-input-events)
  (let [ctx (context 640 480)] 
    (tick ctx (create-state))))

(defn hook-input-events []
  (.addEventListener js/document "keydown" 
   (fn [e]
    (set-key-state (. e -keyCode) true)
     false))
  (.addEventListener js/document "keyup" 
   (fn [e]
    (set-key-state (. e -keyCode) false)
     false)))

(defn set-key-state [code, value]
  (swap! key-states assoc code value))
