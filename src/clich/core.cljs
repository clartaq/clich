;;;;
;;;; Clich is a very simple Rich Text Editor written in ClojureScript.
;;;;

(ns clich.core
  (:require [cljs.pprint :as pp]
            [reagent.core :as r]))

;;;
;;; Utilities.

(defn log [s]
  (.log js/console s))

(defn exec
  ([command]
   (exec command nil))
  ([command value]
   (.execCommand js/document command false value)))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn add-html!
  [id html]
  (set! (.-innerHTML (by-id id)) html))

(defn add-text!
  [id txt]
  (set! (.-textContent (by-id id)) txt))

(defn query-command-state
  [command]
  (.queryCommandState js/document command))

(defn query-command-value
  [command]
  (.queryCommandValue js/document command))

(defn add-listener
  [parent tipe listener]
  (.addEventListener parent tipe listener))

;;;
;;; The editor.
;;;

; The actions associated with the toolbar buttons.
(def actions [{:icon    "<b>B</b>"
               :title   "Bold"
               :state   #(query-command-state "bold")
               :onclick #(exec "bold")}
              {:icon    "<i>I</i>"
               :title   "Italic"
               :state   #(query-command-state "italic")
               :onclick #(exec "italic")}
              {:icon    "<u>U</u>"
               :title   "Underline"
               :state   #(query-command-state "underline")
               :onclick #(exec "underline")}
              {:icon    "<strike>S</strike"
               :title   "Strike-through"
               :state   #(query-command-state "strikeThrough")
               :onclick #(exec "strikeThrough")}
              {:icon    "<b>H<sub>1</sub></b>"
               :title   "Heading 1"
               :onclick #(exec "formatBlock" "<h1>")}
              {:icon    "<b>H<sub>2</sub></b>"
               :title   "Heading 2"
               :onclick #(exec "formatBlock" "<h2>")}
              {:icon    "&#182;"
               :title   "Paragraph"
               :onclick #(exec "formatBlock" "<p>")}
              {:icon    "&#8220; &#8221;"
               :title   "Quote"
               :onclick #(exec "formatBlock" "<blockquote>")}
              {:icon    "&#35;"
               :title   "Ordered List"
               :onclick #(exec "insertOrderedList")}
              {:icon    "&bull;"
               :title   "Unordered List"
               :onclick #(exec "insertUnorderedList")}
              {:icon    "&lt;/&gt;"
               :title   "Code"
               :onclick #(exec "formatBlock" "<pre>")}
              {:icon    "&#8213;"
               :title   "Horizontal Rule"
               :onclick #(exec "insertHorizontalRule")}
              {:icon    "&#128279;"
               :title   "Link"
               :onclick #(let [url (.prompt js/window "Enter the link URL")]
                           (when url (exec "createLink" url)))}
              {:icon    "&#128247;"
               :title   "Image"
               :onclick #(let [url (.prompt js/window "Enter the image URL")]
                           (when url (exec "insertImage" url)))}])

(defn build-toolbar-button
  "Create and initialize a toolbar button and return it."
  [ed-ele m]
  (let [b (.createElement js/document "button")]
    (set! (.-type b) "button")
    (set! (.-className b) "clich-toolbar-button")
    (set! (. b -innerHTML) (:icon m))
    (set! (.-title b) (:title m))
    (when (:state m)
      (let [handler nil]
        (add-listener ed-ele "keyup" handler)
        (add-listener ed-ele "mouseup" handler)
        (add-listener ed-ele "click" handler)))
    (when (:onclick m)
      (set! (.-onclick b) (:onclick m)))
    b))

(defn init-toolbar
  "Initialize the toolbar by adding buttons to it."
  [ed-ele tb-ele]
  (doseq [m actions]
    (let [btn (build-toolbar-button ed-ele m)]
      (.appendChild tb-ele btn))))

(defn init-clich-editor
  "The editor has been rendered by this point. Initialize it's parts."
  [settings]
  (let [tb-ele (by-id (:toolbar-div-id settings))
        ed-ele (by-id (:editor-div-id settings))]
    (when tb-ele
      (init-toolbar ed-ele tb-ele))
    (set! (.-contentEditable ed-ele) true)
    (set! (.-className ed-ele) "clich-editor")
    (set! (.-oninput ed-ele) (fn [inp]
                               ; ok, the inp.target is a div element
                               (let [first-child (.-firstChild (.-target inp))]
                                 (if (and first-child (= (.-nodeType first-child) 3))
                                   (exec "formatBlock"
                                         (str "<" (:default-paragraph-separator settings) ">"))
                                   (when (= "<br>" (.-innerHTML ed-ele))
                                     (set! (.-innerHTML ed-ele) "")))))))
  (add-text! "the-editor" "This is the editor."))

(defn clich-editor
  "Lay out the editor and, possibly, the toolbar."
  [settings]
  [:div {:class "clich-container"}
   (when (:show-toolbar settings)
     [:div {:class (:toolbar-div-class settings) :id (:toolbar-div-id settings)}])
   [:div {:class (:editor-div-class settings) :id (:editor-div-id settings)}]])

;;;
;;; The demo page and settings.
;;;

(def settings {:show-toolbar                true
               :default-paragraph-separator "p"
               :style-with-css              false
               :on-change                   (fn [html] (log "saw a change"))
               :toolbar-div-id              "the-toolbar"
               :toolbar-div-class           "clich-toolbar"
               :editor-div-id               "the-editor"
               :editor-div-class            "clich-editor"})
;               :on-change (fn [html] (add-html ""
;               set! (.-innerHTML (by-id "text-output-div") )))})

(defn demo-page
  "Build and return the demo page markup."
  []
  [:main
   [:header
    [:h2 "Welcome to Clich"]
    [:h3 "A Rich Text Editor written in ClojureScript"]
    [:h4 "This demo is written using Reagent"]]
   [:div {:class "editor-container"}
    (clich-editor settings)]
   [:div {:class "text-output-div"}
    [:h3 "Text output:"]
    [:div {:id "text-output"}]]
   [:div {:class "html-output-div"}
    [:h3 "HTML output:"]
    [:pre {:id "html-output"}]]
   [:footer
    [:div {:class "demo-button-bar"}
     [:button {:class "demo-button"} "Button 1"]
     [:button {:class "demo-button"} "Button 2"]]]])

;;
;; Initialize the app.
;;

(defn mount-root
  "Start 'er up!"
  []
  ; Render the page so we have some DOM to work with.
  (r/render [demo-page] (.getElementById js/document "app"))
  ; Now that we have some DOM, initialize the editor.
  (init-clich-editor settings)
  (add-text! "text-output" "Some text")
  (add-html! "html-output" "Some HTML"))

(defn init! []
  (mount-root))
