(ns app.db)

(def default-db
  {:todos (sorted-map-by >)
   :app-state {:coordinates [1 1 1]
               :comparison {:parts ["18654" "4459"]}
               :part-list ["18654" "4459" "65249" "60483"]}})
