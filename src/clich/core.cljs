;;;;
;;;; Clich is a very simple Rich Text Editor written in ClojureScript.
;;;;

(ns clich.core
  (:require [cljs.pprint :as pp]
            [reagent.core :as r]))

;;;
;;; The editor.
;;;

;;
;; Editor utilities.
;;

(defn log [s]
  (.log js/console s))

(defn exec
  ([command]
   (exec command nil))
  ([command value]
   (.execCommand js/document command false value)))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn query-command-state
  [command]
  (.queryCommandState js/document command))

(defn query-command-value
  [command]
  (.queryCommandValue js/document command))

(defn add-listener
  [parent tipe listener]
  (.addEventListener parent tipe listener))

; Information needed to create the buttons in the toolbar.
(def toolbar-button-info
  [{:icon    "<b>B</b>"
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
  [ed-ele btn-info]
  (let [b (.createElement js/document "button")]
    (set! (.-className b) "clich-toolbar-button")
    (set! (.-innerHTML b) (:icon btn-info))
    (set! (.-title b) (:title btn-info))
    (.setAttribute b "type" "button")
    (when-let [state-query-fn (:state btn-info)]
      (let [handler #(let [ctbs "clich-toolbar-button-selected"
                           btn-cls-lst (.-classList b)]
                       (if (state-query-fn)
                         (.add btn-cls-lst ctbs)
                         (.remove btn-cls-lst ctbs)))]
        (add-listener ed-ele "keyup" handler)
        (add-listener ed-ele "mouseup" handler)
        (add-listener ed-ele "click" handler)))
    (set! (.-onclick b) #(and ((:onclick btn-info) %) (.focus ed-ele)))
    b))

(defn init-toolbar
  "Initialize the toolbar by adding buttons to it."
  [ed-ele tb-ele]
  (doseq [m toolbar-button-info]
    (let [btn (build-toolbar-button ed-ele m)]
      (.appendChild tb-ele btn))))

(defn init-clich-editor
  "The editor has been rendered by this point. Initialize it's parts.
  Return the editor element."
  [settings]
  (let [ed-ele (by-id (:editor-div-id settings))
        ps (or (:default-paragraph-separator settings) "div")]

    ; Set some global document properties.
    (exec "defaultParagraphSeparator" ps)
    (when (:style-with-css settings)
      (exec "styleWithCSS"))

    (when-let [tb-ele (by-id (:toolbar-div-id settings))]
      (init-toolbar ed-ele tb-ele))

    ; Go ahead and initialize the editor element.
    (set! (.-contentEditable ed-ele) true)
    (set! (.-className ed-ele) "clich-editor")
    (set! (.-oninput ed-ele) (fn [evt]
                               (let [first-child (.-firstChild (.-target evt))]
                                 (if (and first-child (= (.-nodeType first-child) 3))
                                   (exec "formatBlock"
                                         (str "<" (:default-paragraph-separator settings) ">"))
                                   (when (= "<br>" (.-innerHTML ed-ele))
                                     (set! (.-innerHTML ed-ele) "")))
                                 (when-let [och (:on-change settings)]
                                   (och (.-innerHTML ed-ele))))))
    (set! (.-onkeydown ed-ele) (fn [evt]
                                 (if (= "Tab" (.-key evt))
                                   (.preventDefault evt)
                                   (when (and (= "Enter" (.-key evt))
                                              (= "blockquote" (query-command-value "formatBlock")))
                                     (.setTimeout js/document (exec "formatBlock"
                                                                    (str "<" ps ">")) 0)))))
    ed-ele))

(defn layout-clich-editor
  "Lay out the editor and, possibly, the toolbar."
  [settings]
  [:div {:class "clich-container" :id "clich-container"}
   (when (:show-toolbar settings)
     [:div {:class (:toolbar-div-class settings) :id (:toolbar-div-id settings)}])
   [:div {:class (:editor-div-class settings) :id (:editor-div-id settings)}]])

;;;
;;; The demo page and settings.
;;;

;;
;; Demo utilities.
;;

(defn add-html!
  [id html]
  (set! (.-innerHTML (by-id id)) html))

(defn add-text!
  [id txt]
  (set! (.-textContent (by-id id)) txt))

;;
;; Settings for the demo.
;;

(def text-output-div-id "text-output")
(def html-output-div-id "html-output")

(def ed-settings {:toolbar-div-id              "the-toolbar"
                  :toolbar-div-class           "clich-toolbar"
                  :editor-div-id               "the-editor"
                  :editor-div-class            "clich-editor"
                  :show-toolbar                true
                  :default-paragraph-separator "p"
                  :style-with-css              false
                  :on-change                   (fn [html]
                                                 (add-html! text-output-div-id html)
                                                 (add-text! html-output-div-id html))})
;;
;; Lay out the demo page.
;;

(defn demo-page
  "Build and return the demo page markup."
  []
  [:main
   [:header
    [:h2 "Welcome to Clich"]
    [:h3 "A Rich Text Editor written in ClojureScript"]
    [:h4 "This demo is written using Reagent"]]
   [:div {:class "editor-container"}
    (layout-clich-editor ed-settings)]
   [:div {:class "text-output-div"}
    [:h3 "Text output:"]
    [:div {:id text-output-div-id}]]
   [:div {:class "html-output-div"}
    [:h3 "HTML output:"]
    [:pre {:id html-output-div-id}]]])

;;;
;;; Initialize the app.
;;;

(defn mount-root
  "Start 'er up!"
  []
  ; Render the page so we have some DOM to work with.
  (r/render [demo-page] (.getElementById js/document "app"))
  (add-text! text-output-div-id "Formatted text will appear here.")
  (add-html! html-output-div-id "Raw HTML will appear here.")
  ; Now that we have some DOM, initialize the editor and put the focus on it.
  (.focus (init-clich-editor ed-settings)))

(defn init! []
  (mount-root))
