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
   [goog.object :as gobj]
   [react-oidc-context :as oidc :refer [AuthProvider useAuth]]
   [react :refer [StrictMode]]))

(def client-id "1f7ud36u0tud5lt9pf7mb6cmoq")
(def redirect_uri "http://localhost:8080/")

(defn on-sign-in-callback []
  (set! (.. js/window -location -href) "/"))

(def cognito-auth-config
  #js {"authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_R56ssR1OX"
       "client_id" client-id
       "redirect_uri" redirect_uri
       "response_type" "code"
       "scope" "openid email"
       "onSigninCallback" on-sign-in-callback});

(defui sign-in-form [{:keys [auth]}] 
  ($ :div
     ($ :h2 "Login")
     ($ :button {:on-click (fn [] (.signinRedirect ^js auth))} "Log in")))

(defui sign-out-form [{:keys [auth]}]
  ($ :div
     ($ :button {:on-click (fn [] (.removeUser ^js auth))} "Logout")))

(defui profile-view [{:keys [auth]}]
  (let [user (.-user auth)
        profile (.-profile user)]
    ($ :div
       ($ :p (str "Logged in as: " (.-email profile))) 
       ($ sign-out-form {:auth auth}))))

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
       (cond
         (.-isAuthenticated auth) ($ profile-view {:auth auth})
         (.-isLoading auth) "Loading..."
         (.-error auth) (str "Error: " (gobj/get auth "error"))
         :else ($ sign-in-form {:auth auth}) 
         ))))

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
