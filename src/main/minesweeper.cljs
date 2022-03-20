(ns main.minesweeper
  (:require [clojure.set :as set]))

(defn config []
  {:canvas-size 400
   :rows 10
   :cols 10
   :cell-size 40
   :bombs-total 20})

(defonce *opened (atom #{}))
(defonce *cells (atom #{}))
(defonce *bombs (atom #{}))
(defonce *resources (atom {}))

(defn valid [[row col]]
  (when (and (< -1 row (:rows (config))) (< -1 col (:cols (config))))
    [row col]))

(defn bombs-around [rowcol bombs]
  (let [nb (for [r [-1 0 1] c [-1 0 1] :when (not= 0 r c)] (map + rowcol [r c]))]
    (->> (set/intersection (set nb) bombs) count)))

(defn cell-new [rowcol bombs cell-size]
  {:rowcol rowcol
   :x (* (second rowcol) cell-size)
   :y (* (first rowcol) cell-size)
   :bomb? (bombs rowcol)
   :bombs-around (bombs-around rowcol bombs)})

(defn flood-fill [rowcol bombs result]
  (when (and (valid rowcol) (not (result rowcol)))
    (if (pos? (bombs-around rowcol bombs))
      (conj result rowcol)
      (as-> (conj result rowcol) Z
        (set/union Z (flood-fill (map + rowcol [-1 0]) bombs Z))
        (set/union Z (flood-fill (map + rowcol [0 1]) bombs Z))
        (set/union Z (flood-fill (map + rowcol [1 0]) bombs Z))
        (set/union Z (flood-fill (map + rowcol [0 -1]) bombs Z))))))

(defn open [[row col] bombs]
  (if (and (zero? (bombs-around [row col] bombs)) (not (bombs [row col])))
    (-> (flood-fill [row col] bombs #{}) (disj nil))
    (set [[row col]])))

(defn game-new [{:keys [cell-size cols rows bombs-total]}]
  (let [rowcols (for [row (range rows) col (range cols)] [row col])
        bombs (->> rowcols shuffle (take bombs-total) set)
        cells (for [rowcol rowcols] (cell-new rowcol bombs cell-size))]
    (reset! *cells cells)
    (reset! *bombs bombs)
    (reset! *opened #{})))

(defn game-lost? [] (not-empty (set/intersection @*bombs @*opened)))

(defn game-won? [] (= (count @*bombs) (- (count @*cells) (count @*opened))))

(defn game-over? [] (or (game-lost?) (game-won?)))

(defn xy->rowcol [x y {:keys [cell-size canvas-size]}]
  (when (and (< 0 x canvas-size) (< 0 y canvas-size))
    (valid [(quot y cell-size) (quot x cell-size)])))

(defn mousePressed []
  (when-let [rowcol (xy->rowcol js/mouseX js/mouseY (config))]
    (cond
      (game-over?) (game-new (config))
      (@*bombs rowcol) (reset! *opened (-> (map :rowcol @*cells) set))
      :else
      (->> (open rowcol @*bombs) (swap! *opened set/union)))
    (js/redraw)))

(defn setup []
  (game-new (config))
  (let [canvas-size (:canvas-size (config))
        canvas (js/createCanvas canvas-size canvas-size)]
    (.parent canvas "app")))

(defn cell-type [{:keys [bomb? bombs-around]}]
  (cond
    bomb? "bomb"
    (zero? bombs-around) "blank"
    :else "count"))

(defmulti draw-cell cell-type)

(defmethod draw-cell "bomb" [{:keys [x y]} cell-size]
  (let [margin 5
        size (- cell-size (* 2 margin))
        image-type (if (game-won?) :bomb-won :bomb-lost)
        img (image-type @*resources)]
    (js/image img (+ x margin) (+ y margin) size size)))

(defmethod draw-cell "count" [{:keys [x y bombs-around]} cell-size]
  (js/textSize 16)
  (js/textAlign js/CENTER js/CENTER)
  (js/fill 0 0 0)
  (let [offset (* 0.5 cell-size)]
    (js/text (str bombs-around) (+ offset x) (+ offset y))))

(defmethod draw-cell "blank" [_ _])

(defn draw-button [{:keys [x y]} cell-size]
  (js/fill 81)
  (js/rect x y cell-size cell-size))

(defn safe-draw [f & args]
  (js/push)
  (apply f args)
  (js/pop))

(defn draw []
  (let [cell-size (:cell-size (config))]
    (js/fill 170)
    (doseq [{:keys [x y rowcol] :as cell} @*cells]
      (js/rect x y cell-size cell-size)
      (if (or (@*opened rowcol) (and (game-over?) (@*bombs rowcol)))
        (safe-draw draw-cell cell cell-size)
        (safe-draw draw-button cell cell-size)))
    (js/noLoop)))

(defn preload []
  (let [bomb-won (js/loadImage "/images/app/smiley.png")
        bomb-lost (js/loadImage "/images/app/explosion.png")]
    (swap! *resources assoc :bomb-won bomb-won :bomb-lost bomb-lost)))

(defn init []
  (doto js/window
    (aset "setup" setup)
    (aset "draw" draw)
    (aset "mousePressed" mousePressed)
    (aset "preload" preload)))

(defn dev-before-load [])

(defn dev-after-load []
  (init)
  (js/redraw))