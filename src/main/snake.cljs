(ns main.snake)

(def canvas-size 450)
(def grid-size 15)
(def snake-colors ["#E50000" "#FF8D00" "#FFEE00" "#028221" "#004CFF" "#770088"])

(def rows (int (Math/floor (/ canvas-size grid-size))))
(def cols rows)

(defonce *snake (atom nil))
(defonce *food (atom nil))

(defn out-of-bounds? [{:keys [x y]}]
  (not (and (< -1 x cols) (< -1 y rows))))

(defrecord Food [xy])

(defn new-food []
  (->Food {:x (rand-int cols) :y (rand-int rows)}))

(defn update-food [food snake-head]
  (if (= (:xy food) snake-head) (new-food) food))

(defn draw-food [{:keys [xy]}]
  (js/push)
  (js/fill 255 0 100)
  (let [gs grid-size]
    (js/rect (* gs (:x xy)) (* gs (:y xy)) gs gs))
  (js/pop))

(defrecord Snake [x-dir y-dir segments colors])

(defn new-snake []
  (->Snake 1 0 (list {:x 0 :y 0}) (list (first snake-colors))))

(defn whereto [{:keys [x-dir y-dir segments]}]
  (-> segments first (update :x + x-dir) (update :y + y-dir)))

(defn hits-body? [{:keys [segments]} next-head]
  (some (partial = next-head) (rest segments)))

(defn reverse? [{:keys [x-dir y-dir]} & new-dir]
  (= [x-dir y-dir] (map (partial * -1) new-dir)))

(defn colorize [{:keys [segments] :as snake}]
  (let [len (count segments)]
    (assoc snake :colors (take len (cycle snake-colors)))))

(defn grow [snake next-head]
  (-> snake
      (update :segments conj next-head)
      colorize))

(defn move [snake next-head]
  (-> snake
      (update :segments conj next-head)
      (update :segments butlast)))

(defn change-direction [snake new-x-dir new-y-dir]
  (if (reverse? snake new-x-dir new-y-dir)
    snake
    (assoc snake :x-dir new-x-dir :y-dir new-y-dir)))

(defn update-snake [snake next-head food]
  (cond
    (out-of-bounds? next-head) (new-snake)
    (hits-body? snake next-head) (new-snake)
    (= next-head (:xy food)) (grow snake next-head)
    :else (move snake next-head)))

(defn draw-snake [{:keys [segments colors]}]
  (js/push)
  (let [gs grid-size]
    (doseq [[seg col] (map vector segments colors)]
      (js/fill col)
      (js/circle (* gs (:x seg)) (* gs (:y seg)) gs))
  (js/pop)))

(defn draw-score [{:keys [segments]}]
  (js/push)
  (js/textSize (* 2 grid-size))
  (js/textAlign js/RIGHT)
  (js/textStyle js/BOLD)
  (js/fill "rgba(0,0,0,0.6)")
  (js/text (str (dec (count segments))) (- canvas-size (* 0.5 grid-size)) (* 2 grid-size))
  (js/pop))

(defn new-game []
  (reset! *snake (new-snake))
  (reset! *food (new-food)))

(defn keyPressed []
  (let [change-dir
        (fn [x-dir y-dir]
          (swap! *snake change-direction x-dir y-dir)
          false)]
    (condp = js/keyCode
      js/UP_ARROW (change-dir 0 -1)
      js/RIGHT_ARROW (change-dir 1 0)
      js/DOWN_ARROW (change-dir 0 1)
      js/LEFT_ARROW (change-dir -1 0)
      nil)))

(defn setup []
  (new-game)
  (js/frameRate 15)
  (js/ellipseMode js/CORNER);
  (let [canvas (js/createCanvas canvas-size canvas-size)]
    (.parent canvas "app")))

(defn draw []
  (js/background 170)
  (let [next-head (whereto @*snake)]
    (swap! *snake update-snake next-head @*food)
    (swap! *food update-food next-head))
  (draw-score @*snake)
  (draw-food @*food)
  (draw-snake @*snake))

(defn init []
  (doto js/window
    (aset "setup" setup)
    (aset "draw" draw)
    (aset "keyPressed" keyPressed)))

(defn dev-before-load [])

(defn dev-after-load []
  (new-game)
  (init))