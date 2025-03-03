(ns app.core
  (:require
   [uix.core :as uix :refer [defui $]]
   [uix.dom]
   [app.hooks :as hooks]
   [app.subs]
   [app.handlers]
   [app.fx]
   [app.db]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [goog.string :as gs]
   [goog.string.format]
   [emmy.calculus.manifold :as manifold]
   [emmy.env :as emmy]
   [react-oidc-context :as oidc :refer [AuthProvider]]
   [react :refer [StrictMode]]))

(def cognito-auth-config 
  #js {
       "authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_0b51ky6YU" 
       "client_id" "67lmgncc2h7770qlgbtav0df6v" 
       "redirect_uri" "https://www.surprisebuild.com" 
       "response_type" "code" 
       "scope" "openid",
});

(defui header []
  ($ :header.app-header
     ($ :div {:width 32}
        ($ :p {:style {:font-family "Montserrat" :font-size 48}} "surprisebuild"))))

(defui footer []
  ($ :footer.app-footer
     ($ :small "made by Daniel Craig")))

(defui manifold-point-viewer []
  (let [manifold-point (hooks/use-subscribe [:app/manifold-point])]
    ($ :manifold-point-viewer 
       (str manifold-point))))

(defui coordinate-field [{:keys [on-edit i]}]
  (let [displacement (hooks/use-subscribe [:app/coordinate i])]
    ($ :div
       ($ :input
          {:value displacement
           :type :number
           :min 1
           :max 400
           :placeholder 0
           :style {:width "80%"}
           :on-change (fn [^js e]
                        (on-edit (int (.. e -target -value))))}))))

(defui authenticated-app []
  ($ StrictMode
     ($ AuthProvider
        cognito-auth-config
        ($ :div
           "authenticated, hello world"))))
  

(defui app []
  (let [todos (hooks/use-subscribe [:app/todos])]
    ($ :.app
       ($ header) 
       ($ authenticated-app)
       ($ footer))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (rf/dispatch-sync [:app/init-db app.db/default-db])
  (uix.dom/render-root ($ app) root))

(defn ^:export init []
  (render))
