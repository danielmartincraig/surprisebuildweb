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
   [emmy.calculus.manifold :as manifold]
   [emmy.env :as emmy]
   [goog.object :as gobj]
   [react-oidc-context :as oidc :refer [AuthProvider useAuth]]
   [react :refer [StrictMode]]
   [formik :refer [Formik Field Form]]))

(def client-id "1f7ud36u0tud5lt9pf7mb6cmoq")
(def redirect_uri "https://www.surprisebuild.com/")

(def cognito-auth-config
  #js {"authority" "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_R56ssR1OX"
       "client_id" client-id
       "redirect_uri" redirect_uri
       "response_type" "code"
       "scope" "openid profile email"});

(defui sign-in-form [] 
  ($ :div
     ($ :h2 "Login")
     ($ :a {:href (str "https://authsurprisebuild.auth.us-east-1.amazoncognito.com/login?client_id=" client-id "&response_type=code&scope=email+openid&redirect_uri=" (js/encodeURIComponent redirect_uri))} "Login")
     ($ :p "Don't have an account?  Click the 'Sign Up' button to create one.")
     ($ :a {:href (str "https://authsurprisebuild.auth.us-east-1.amazoncognito.com/signup?client_id=" client-id "&response_type=code&scope=email+openid&redirect_uri=" (js/encodeURIComponent redirect_uri))} "Sign Up")))

(defui sign-out-form []
  ($ :div
     ($ :button {:on-click client/sign-out-redirect} "Logout")))

(defui sign-up-form [auth]
  ($ :div
     ($ Formik
        {:initial-values #js {:password "" :email ""}
         :onSubmit (fn [values] 
                     (let [password (gobj/get values "password")
                           email (gobj/get values "email")] 
                       (rf/dispatch [:app/handle-sign-up password email])))}
        ($ Form 
           ($ Field {:name "email" :type "email" :placeholder "Email"})
           ($ Field {:name "password" :type "password" :placeholder "Password"})
           ($ :button {:type "submit"} "Sign Up")))))

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
            :else ($ sign-in-form auth))))))

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
