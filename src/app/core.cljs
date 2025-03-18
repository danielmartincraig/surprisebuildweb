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
   [react :refer [StrictMode]]
   [shadow.cljs.modern :refer (js-await)]
   [formik :refer [Formik Field Form]]))

(def cognito-auth-config
  #js {"authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_R56ssR1OX"
       "client_id" "1f7ud36u0tud5lt9pf7mb6cmoq"
       "redirect_uri" "http://localhost:8080/"
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

(defn sign-up [client-id username password email]
  (let [client (CognitoIdentityProviderClient.)
        sign-up-command (SignUpCommand. (clj->js {:ClientId client-id
                                                  :Username username
                                                  :Password password
                                                  :UserAttributes [{:Name "email" :Value email}]}))]
    (.send client sign-up-command)))

(defn sign-up-handler [[_ username password email]]
  (let [clientId "1f7ud36u0tud5lt9pf7mb6cmoq"]
    (when (validate-user username password email)
      (rf/console :log (str "Signing up user: " username))
      (js-await (sign-up clientId username password email)))))

(defui sign-out-form []
  ($ :div
     ($ :button {:on-click sign-out-redirect} "Logout")))

(defui sign-up-form [auth]
  ($ :div
     ($ Formik
       {:initial-values {:username "" :password "" :email ""}
         :onSubmit (fn [values] 
                     (rf/console :log (str "Submitting sign-up form with values: " values))
                     (sign-up-handler (vals values)))}
        (fn [props]
          ($ Form
             ($ Field {:name "username" :placeholder "Username"})
             ($ Field {:name "password" :type "password" :placeholder "Password"})
             ($ Field {:name "email" :type "email" :placeholder "Email"})
             ($ :button {:type "submit"} "Sign Up"))))))

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
            (gobj/get auth "isAuthenticated") ($ sign-out-form)
            (gobj/get auth "isLoading") "Loading..."
            (gobj/get auth "error") (str "Error: " (gobj/get auth "error"))
            :else ($ sign-up-form auth))))))

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
