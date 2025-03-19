(ns app.handlers
  (:require [re-frame.core :as rf]
            [app.db :as db]
            [app.client :as client]
            [re-promise.core]
            [shadow.cljs.modern :refer (js-await)]
            [app.fx :as fx]))

(def load-app-state (rf/inject-cofx :store/app-state "surprisebuildweb/app-state"))
(def store-app-state (fx/store-app-state "surprisebuildweb/app-state"))

(rf/reg-event-fx :app/init-db
                 [load-app-state]
                 (fn [{:store/keys [app-state]} [_ default-db]]
                   {:db (update default-db :app-state into app-state)}))

(rf/reg-fx :app/sign-up
           (fn [client-id username password email]
             #_(rf/console :log (str "sign-up: signing up user " username))
             (js-await (client/sign-up client-id username password email))))

(rf/reg-event-fx :app/sign-up-using-promise
                 (fn [{:keys [db] :as cofx} [_ client-id username password email]]
                   (let [client-id "1f7ud36u0tud5lt9pf7mb6cmoq"]
                     (rf/console :log (str "handle-sign-up: signing up user " username))
                     {:promise {:call #(-> (client/sign-up client-id username password email)
                                           (.then (fn [response]
                                                    (rf/console :log (str response)))))
                                ;;:on-success [:your-success-handler "some-str"]
                                ;;:on-failure [:your-failure-handler {:some :map}]
                                }})))

(rf/reg-event-fx :app/handle-sign-up
                 (fn [{:keys [db] :as cofx} [_ username password email]] 
                   (let [client-id "1f7ud36u0tud5lt9pf7mb6cmoq"]
                     (rf/console :log (str "handle-sign-up: signing up user " username))
                     {:fx [[:dispatch [:app/sign-up-using-promise client-id username password email]]]})))

(rf/reg-event-db :coordinates/update-coordinates
                 [store-app-state]
                 (fn [db [_ i new-coordinate-value]]
                   (if (<= (abs new-coordinate-value) 15)
                     (assoc-in db [:app-state :coordinates i] new-coordinate-value)
                     db)))

(rf/reg-event-db :coordinates/reset-app-db
                 [store-app-state]
                 (fn [db _]
                   db/default-db))