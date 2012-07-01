(ns unlambda-clj.translator
  (:require [unlambda-clj.core :as core]
            [clojure.java.shell :as shell]
            [clojure.walk :as walk]))


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

(def ^{:doc "Atom that contains all user defined functions. It is saved to file on every change in order to restore them later."}
  fns-db
  (atom (get-functions-from-db)))

(def ^{:doc "Atom that contains function translations, so we don't translate function to unlambda every time we call it."}
  fns-translations
  (atom {}))

; Add watcher to save all functions to file every time we define/undefine function.
(add-watch fns-db :auto-save
           (fn [_ _ _ value] (spit db-file (pr-str value))))

(defn built-in?
  "Check if current function is built-in unlambda function, such as i, k, s, .*"
  [fn]
  (->> fn str first #{\k \s \i \v \c \d \e \@ \| \. \?} boolean))

(declare translate-fn)
(declare translate-exp)

(defn get-translation
  "Get translation for given function. If function is not defined it will be returned unchanged."
  [fn]
  (or (@fns-translations fn)
      (if-let [{:keys [args body]} (@fns-db fn)]
        (let [translation (translate-fn args body)]
          (swap! fns-translations assoc fn translation)
          translation)
        fn)))

(defn eliminate-var
  "Eliminates variable (argument) from unlambda expression. See http://www.madore.org/~david/programs/unlambda/#lambda_elim"
  [exp var]
  (mapcat #(cond (= % bq) [bq bq 's]
                 (= % var) ['i]
                 :else [bq 'k %])
          exp))

(defn translate-exp
  "Translates expression to unlambda. Returns sequence of symbols."
  [exp]
  (cond (coll? exp)
        (let [[f & rst] exp]
          (if (= 'fn f)
            (apply translate-fn rst)
            (flatten
             (concat (repeat (count rst) bq)
                     (map translate-exp exp)))))
        (string? exp) (map (comp symbol str) exp)
        :else (get-translation exp)))

(defn translate-fn
  "Translated function to unlambda. Translates function body to unlambda and then eliminates all arguments one by one in reverse order."
  [args body]
  (let [exp (translate-exp body)]
    (reduce eliminate-var
            (if (coll? exp) exp [exp])
            (reverse args))))

(defn to-unlambda-sym
  "Translates expression to unlambda. Returnes string."
  [exp]
  (apply str (translate-exp exp)))

(defmacro defu
  "Defines function that will translated to unlambda.
  Usage:
  (defu print-number [n] (n .* i)) - define function that
  prints passed number to standard output."
  [name args body]
  `(do (swap! fns-db assoc (quote ~name) {:args (quote ~args) :body (quote ~body)})
       (reset! fns-translations {})
       (quote ~name)))

(defmacro undefu
  "Undefines function. Deletes it."
  [name]
  `(do (swap! fns-db dissoc (quote ~name))
       (quote ~name)))


(defmacro to-unlambda
  "Handy macro to avoid quoting expression."
  [exp]
  `(to-unlambda-sym (quote ~exp)))

(defmacro run-unlambda
  "Translates given expression to unlambda and run it.
  Writes output to *out*.
  Usage: (run-unlambda (.o (.l (.l (.e (.H i))))))
  writes \"Hello\" to *out*"
  [exp]
  `(eval-native (to-unlambda-sym (quote ~exp))))

(defn show-fns
  "Prints all user defined functions to *out* with args and bodies"
  []
  (doseq [[name {:keys [args body]}] @fns-db]
    (println (list name args body))))


