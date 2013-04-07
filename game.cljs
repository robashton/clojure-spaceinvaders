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

(defn firing-rate [state] (min 15 (- 30 (* 2 (:level state)))))
(defn enemy-speed [state] (:level state))
(defn bullet-speed [state](:level state))
(defn player-speed [state] (* 2 (:level state)))
(defn enemy-descent-speed [state] 25)
(defn rect-right [rect] (+ (:x rect) (:w rect)))
(defn rect-bottom [rect] (+ (:y rect) (:h rect)))

(defn create-state [level]
 { :direction 1
   :level level
   :enemies (for [x (range 0 480 60)
                  y (range 0 240 60)]
              (create-rect x y 20 20))
   :player (create-rect 200 430 20 20)
   :bullets () 
   :last-firing-ticks 0})

(defn rects-max-x [rects]
  (apply max (map :x rects)))

(defn rects-min-x [rects]
  (apply min (map :x rects)))

(defn enemies-reached-edge [enemies direction]
  (cond (and (= direction 1) (> (rects-max-x enemies) 600)) true
        (and (= direction -1) (< (rects-min-x enemies) 0)) true
        :else false))

(defn invert-enemies-direction [state]
  (assoc state 
         :direction (* (:direction state) -1)
         :enemies (map 
                    (fn [enemy] (assoc enemy :y (+ (:y enemy) (enemy-descent-speed state))))
                      (:enemies state))))

(defn update-direction [state]
  (if (enemies-reached-edge (:enemies state) (:direction state))
    (invert-enemies-direction state) state))

(defn update-enemies [state]
  (let [direction (:direction state)
        enemies (:enemies state)
        func (if(= direction 1) 
               #(+ % (enemy-speed state)) 
               #(- % (enemy-speed state)))]
    (assoc state :enemies
      (for [enemy enemies]
        (update-in enemy [:x] func)))))

(defn update-firing-ticks [state]
  (if (= (:last-firing-ticks state) 0) 
    state
    (if (= (rem (:last-firing-ticks state) (firing-rate state)) 0)
      (assoc state :last-firing-ticks 0)
      (update-in state [:last-firing-ticks] inc))))

(defn update-bullets [state]
  (try-and-fire
    (update-firing-ticks
      (collide-bullets
        (move-bullets state)))))

(defn move-bullets [state]
  (assoc-in state [:bullets]
    (for [bullet (:bullets state)]
      (update-in bullet [:y] #(- % (bullet-speed state))))))

(defn increment-firing-ticks [state]
  (assoc state :last-firing-ticks 1)
)

(defn add-bullet-in-player-location [state]
  (let [player (:player state)]
    (assoc-in state [:bullets]
      (cons 
        (create-rect (:x player) (:y player) 5 5)
        (:bullets state)))))


(defn collides-with [one two]
  (cond (< (rect-right one) (:x two)) false
        (> (:x one) (rect-right two)) false
        (< (rect-bottom one) (:y two)) false
        (> (:y one) (rect-bottom two)) false
        :else true))

(defn collide-bullets [state]
  (assoc 
    (assoc-in state [:bullets]
      (remove #(collides-with-any % (:enemies state)) (:bullets state)))
    :enemies
      (remove #(collides-with-any % (:bullets state)) (:enemies state))))

(defn collides-with-any [one, others]
  (some #(collides-with % one) others))

(defn fire [state]
  (increment-firing-ticks
    (add-bullet-in-player-location state)))

(defn can-fire [state]
  (= (:last-firing-ticks state) 0))

(defn try-and-fire [state]
  (if (and (@key-states 32) (can-fire state))
    (fire state) state))

(defn update-player [state]
  (let [left (@key-states 37)
        right (@key-states 39)]
    (cond (= left true) (update-in state [:player :x] #(- % (player-speed state)))
          (= right true) (update-in state [:player :x] #(+ % (player-speed state)))
          :else state)))

(defn render-rects [ctx rects colour]
  (doseq [rect rects] 
    (render-rect ctx rect colour)))

(defn render-enemies [ctx state]
  (render-rects ctx (:enemies state) "#FF0"))

(defn render-bullets [ctx state]
  (render-rects ctx (get-in state [:bullets]) "#000"))

(defn render-player [ctx state]
  (render-rect ctx (:player state) "#F00"))

(defn validate-end-conditions [state]
  (cond (enemies-are-all-dead (:enemies state)) (start-next-level state)
        (enemies-are-at-the-gate (:enemies state)) (show-game-over)
        :else state))

(defn enemies-are-at-the-gate [enemies]
  (> (apply max (map :y enemies)) 400))

(defn show-game-over []
  (set! (. js/document -location) "gameover.html"))

(defn enemies-are-all-dead [enemies]
  (not (first enemies)))

(defn start-next-level [state]
  (create-state (inc (:level state))))

(defn update-state [state]
  (validate-end-conditions
    (update-bullets
      (update-player
        (update-enemies
          (update-direction state))))))

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
    (tick ctx (create-state 1))))

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
