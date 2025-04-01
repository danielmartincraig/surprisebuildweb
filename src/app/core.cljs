(ns app.core
  (:require
   [uix.core :as uix :refer [defui $]]
   [uix.dom]
   [app.hooks :as hooks]
   [app.subs]
   [app.handlers]
   [app.client :as client]
   [app.fx]
   [app.db]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [goog.string :as gs]
   [goog.string.format]
   [goog.object :as gobj]
   [react-oidc-context :as oidc :refer [AuthProvider useAuth]]
   [react :refer [StrictMode]]))

(def client-id "1f7ud36u0tud5lt9pf7mb6cmoq")
(def redirect_uri "https://www.surprisebuild.com/")

(def cognito-auth-config
  #js {"authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_R56ssR1OX"
       "client_id" client-id
       "redirect_uri" redirect_uri
       "response_type" "code"
       "scope" "openid email"});

(defui sign-in-form [auth] 
  ($ :div
     ($ :h2 "Login")
     ($ :button {:on-click (fn [] (.signinRedirect ^js auth))} "Log in")))

(defui sign-out-form []
  ($ :div
     ($ :button {:on-click client/sign-out-redirect} "Logout")))

(defui header []
  ($ :header.app-header
     ($ :div {:width 32}
        ($ :p {:style {:font-family "Montserrat" :font-size 48}} "surprisebuild"))))

(defui footer []
  ($ :footer.app-footer
     ($ :small "made by Daniel Craig")))

(defui authenticated-app []
  (let [auth (useAuth)]
    ($ :div
       ($ :div
          (cond
            (.-isAuthenticated auth) ($ sign-out-form)
            (.-isLoading auth) "Loading..."
            (.-error auth) (str "Error: " (gobj/get auth "error"))
            ;;:else ($ sign-in-form auth)
             :else (.signinRedirect auth)
            )))))

(defui app []
  (let [todos (hooks/use-subscribe [:app/todos])]
    ($ StrictMode
       ($ AuthProvider
          cognito-auth-config
          ($ :.app
             ($ header)
             ($ authenticated-app)
             ($ footer))))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (rf/dispatch-sync [:app/init-db app.db/default-db])
  (uix.dom/render-root ($ app) root))

(defn ^:export init []
  (render))
