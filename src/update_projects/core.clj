(ns update-projects.core
  (:gen-class)
  (require [rewrite-clj.zip :as z]))

(defn- inc-version [version is-cut?]
  (let [inced (clojure.string/replace version #"\d+(?=-)" #(str (inc (Long/parseLong %))))
        cut-adjusted (if is-cut?
                       (clojure.string/replace inced #"-SNAPSHOT" "")
                       (clojure.string/replace inced #"(?<=\d)$" "-SNAPSHOT"))]
    (println "Duh" is-cut? inced cut-adjusted)
    cut-adjusted))

(defn- update-deps [is-cut? versions project-path]
  (dosync
   (let [filename (str "/Users/gregor/" project-path "/project.clj")
         data (z/of-file filename)
         project-name (-> (z/find-value data z/next 'defproject) z/right z/sexpr)
         version (-> (z/find-value data z/next 'defproject) (z/find-value :description) z/left)
         version-inced (inc-version (z/sexpr version) is-cut?)
         replaced (ref (z/edn (z/root (z/replace version version-inced))))]
     (doseq [[project version] @versions]
       (when-let [prj (z/find-value @replaced z/next (symbol project))]
         (alter replaced (constantly (z/edn (z/root (z/replace (z/right prj) version)))))))
     (swap! versions assoc project-name version-inced)
     (spit filename (z/->root-string @replaced)))))

(defn -main
  "for everything in the args, inc its version number and make any of the following args that require it use the new version"
  [& args]
  (let [versions (atom {})
        [arg1 & projects] args
        is-cut? (= arg1 "cut")]
    (doseq [arg projects]
      (update-deps is-cut? versions arg))
    (when is-cut?
      (println "Cutting non-snapshot versions!"))
    (println @versions)))
