(ns main.core
  (:require [main.snake :as demo]))

;; Init function that is exported into global space.
;; Called from index.html with main.core.init_BANG_()
(defn ^:export init! []
  (demo/init))

;; Automatically called before live reload loads new code.
(defn ^:dev/before-load dev-before-load []
  (demo/dev-before-load))

;; Automatically called after live reload has loaded code.
(defn ^:dev/after-load dev-after-load []
  (demo/dev-after-load))
