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
   [goog.object :as gobj]
   [react-oidc-context :as oidc :refer [AuthProvider useAuth]]
   ["@aws-sdk/client-cognito-identity-provider" :as cognito :refer [SignUpCommand, CognitoIdentityProviderClient]]
   [react :refer [StrictMode]]))

(def cognito-auth-config
  #js {"authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_R56ssR1OX"
       "client_id" "1f7ud36u0tud5lt9pf7mb6cmoq"
       "redirect_uri" "https://www.surprisebuild.com/"
       "response_type" "code"
       "scope" "openid profile email"});

(defn sign-out-redirect []
  (let [clientId "67lmgncc2h7770qlgbtav0df6v"
        logoutUri "https://www.surprisebuild.com/"
        cognitoDomain "https://authsurprisebuild.auth.us-east-1.amazoncognito.com"]
    (set! (.. js/window -location -href)
          (str cognitoDomain "/logout?client_id=" clientId "&logout_uri=" (js/encodeURIComponent logoutUri)))))

(defn validate-user [username password email]
  (and username password email))

(defn sign-up-handler [[_ username password email]]
  (let [clientId "1f7ud36u0tud5lt9pf7mb6cmoq"]
    (when (validate-user username password email)
      (rf/console :log (str "Signing up user: " username))

      )))

(defui header []
  ($ :header.app-header
     ($ :div {:width 32}
        ($ :p {:style {:font-family "Montserrat" :font-size 48}} "surprisebuild"))))

(defui footer []
  ($ :footer.app-footer
     ($ :small "made by Daniel Craig")))

(defui authenticated-app []
  (let [auth (useAuth)]
    (re-frame.core/console :log (str "auth: " auth))
    ($ :div
       ($ :button {:on-click (gobj/get auth "signinRedirect")} "Login")
       ($ :button {:on-click sign-out-redirect} "Logout")
       ($ :div
          (cond
            (gobj/get auth "isAuthenticated") "Authenticated"
            (gobj/get auth "isLoading") "Loading..."
            (gobj/get auth "error") (str "Error: " (gobj/get auth "error"))
            :else "Not Authenticated")))))
  
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
