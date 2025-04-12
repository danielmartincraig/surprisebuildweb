(ns app.db)

(def default-db
  {:todos (sorted-map-by >)
   :app-state {:coordinates [1 1 1]
               :comparison {:parts {:a 32523
                                    :b 3673}}}})
