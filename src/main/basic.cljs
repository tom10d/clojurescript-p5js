(ns main.basic)

(defn setup []
  (let [canvas (js/createCanvas 400 400)]
    (.parent canvas "app")))

(defn draw-square []
  (js/push)
  (js/fill 0 255 255)
  (js/rect 200 200 50 50)
  (js/pop))

(defn draw-circle []
(js/push)
(js/fill 255 100 0)
(js/circle 50 50 30)
(js/pop))

(defn draw []
  (js/background 51)
  (draw-square)
  (draw-circle)
  (js/noLoop))

(defn init []
  (doto js/window
    (aset "setup" setup)
    (aset "draw" draw)))

(defn dev-before-load [])

(defn dev-after-load []
  (init)
  (js/redraw))