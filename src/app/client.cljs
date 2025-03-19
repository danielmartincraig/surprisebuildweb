(ns app.client
  (:require
   ["@aws-sdk/client-cognito-identity-provider" :as cognito :refer [SignUpCommand, CognitoIdentityProviderClient]]
   [re-frame.core :as rf]))

(defn sign-up [client-id username password email]
  (let [client (CognitoIdentityProviderClient. #js {}) 
        sign-up-command (SignUpCommand.  #js {:ClientId client-id
                                              :Username username
                                              :Password password
                                              :UserAttributes [#js {:Name "email" :Value email}]})]
    (rf/console :log (str "signing up user " username))
    (.send client sign-up-command)))

(defn sign-out-redirect []
  (let [clientId "67lmgncc2h7770qlgbtav0df6v"
        logoutUri "https://www.surprisebuild.com/"
        cognitoDomain "https://authsurprisebuild.auth.us-east-1.amazoncognito.com"]
    (set! (.. js/window -location -href)
          (str cognitoDomain "/logout?client_id=" clientId "&logout_uri=" (js/encodeURIComponent logoutUri)))))
