(ns app.client
  (:require
   [re-frame.core :as rf]))

(defn sign-out-redirect []
  (let [clientId "1f7ud36u0tud5lt9pf7mb6cmoq"
        logoutUri "https://www.surprisebuild.com/"
        cognitoDomain "https://authsurprisebuild.auth.us-east-1.amazoncognito.com"]
    (set! (.. js/window -location -href)
          (str cognitoDomain "/logout?client_id=" clientId "&logout_uri=" (js/encodeURIComponent logoutUri)))))
