(ns app.client
  (:require
   [re-frame.core :as rf]))

(defn sign-out-redirect []
  (let [clientId "67lmgncc2h7770qlgbtav0df6v"
        logoutUri "https://www.surprisebuild.com/"
        cognitoDomain "https://authsurprisebuild.auth.us-east-1.amazoncognito.com"]
    (set! (.. js/window -location -href)
          (str cognitoDomain "/logout?client_id=" clientId "&logout_uri=" (js/encodeURIComponent logoutUri)))))
