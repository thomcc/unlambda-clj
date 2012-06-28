(ns unlambda-clj.translator
  (:require [unlambda-clj.core :as core]
            [clojure.java.shell :as shell]))


(def db-file  (java.io.File. "fns-database.clj"))

(def unlambda-native-path "/home/nikelandjelo/unlambda-2.0.0/c-refcnt/unlambda")

(def bq (symbol "`"))

(defn eval-native [str]
  (->> (shell/sh unlambda-native-path :in str)
       :out
       println))



(defn get-functions-from-db []
  (if (.exists db-file)
    (read-string (slurp db-file))
    {}))

(def fns-db (atom (get-functions-from-db)))
(def fns-translations (atom {}))

(add-watch fns-db :auto-save
           (fn [_ _ _ value] (spit db-file (pr-str value))))

(defn built-in? [fn]
  (->> fn str first #{\k \s \i \v \c \d \e \@ \| \. \?} boolean))

(declare to-unlambda-sym)
(declare translate-fn)

(defn get-translation [fn]
  (or (@fns-translations fn)
      (when-let [{:keys [args body]} (@fns-db fn)]
        (let [translation (translate-fn args body)]
          (swap! fns-translations assoc fn translation)
          translation))))

(defn flatten-exp [exp]
  (if (coll? exp)
    (concat (repeat (dec (count exp)) bq)
            (mapcat flatten-exp exp))
    [exp]))

(defn substitute-fns [exp]
  (let [fns @fns-db]
    (mapcat #(if-let [tran (get-translation %)]
               tran
               [%])
            exp)))

(defn eliminate-var [exp var]
  (mapcat #(cond (= % bq) [bq bq 's]
                 (= % var) ['i]
                 :else [bq 'k %])

          exp))

(defn translate-exp [exp]
  (substitute-fns (flatten-exp exp)))

(defn translate-fn [args body]
  (let [exp (translate-exp body)]
    (reduce eliminate-var exp (reverse args))))

(defn to-unlambda-sym [exp]
  (apply str (translate-exp exp)))

(defmacro defu [name args body]
  `(do (swap! fns-db assoc (quote ~name) {:args (quote ~args) :body (quote ~body)})
       (quote ~name)))

(defmacro undefu [name]
  `(do (swap! fns-db dissoc (quote ~name))
       (quote ~name)))


(defmacro to-unlambda [exp]
  `(to-unlambda-sym (quote ~exp)))

(defmacro run-unlambda [exp]
  `(eval-native (to-unlambda-sym (quote ~exp))))

(defn show-fns []
  (doseq [[name {args :args}] @fns-db]
    (println name args)))

