(ns update-projects.core
  (:gen-class)
  (require [rewrite-clj.zip :as z]))

(defn- inc-version [version release-type]
  (let [stripped (clojure.string/replace version #"-[a-zA-Z0-9-]*SNAPSHOT" "")
        inced (clojure.string/replace stripped #"\d+$" #(str (inc (Long/parseLong %))))
        cut-adjusted (case release-type
                       :freeze inced
                       :snap (str inced "-SNAPSHOT")
                       :branch (str inced "-BRANCH-" (rand-int 1000) "-SNAPSHOT"))]
    cut-adjusted))

(defn- update-deps [release-type versions project-path]
  (dosync
   (let [filename (str "/home/gregor/" project-path "/project.clj")
         data (z/of-file filename)
         project-name (-> (z/find-value data z/next 'defproject) z/right z/sexpr)
         version (-> (z/find-value data z/next 'defproject) (z/find-value :description) z/left)
         version-inced (inc-version (z/sexpr version) release-type)
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
        release-type (case arg1
               "cut" :freeze
               "freeze" :freeze
               "branch" :branch
               "snap" :snap
               :snap)]
    (doseq [arg projects]
      (update-deps release-type versions arg))
    (println @versions)))
