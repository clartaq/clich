(ns ^:figwheel-no-load clich.dev
  (:require
    [clich.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
