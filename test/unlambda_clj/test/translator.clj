(ns unlambda-clj.test.translator
  (:use [unlambda-clj.translator])
  (:use [clojure.test])
  (:require [clojure.string]))

(defmacro run [exp]
  `(binding [*out* (java.io.StringWriter.)]
     (run-unlambda ~exp)
     (clojure.string/trim (str *out*))))

(defu + [n m f x] (n f (m f x)))
(defu * [n m f] (n (m f)))
(defu 0 [f x] x)
(defu 1 [f x] (f x))
(defu 2 [f x] (f (f x)))
(defu print [n] (n .* i))
(defu three-as-str [] "``s``s`ksk``s``s`kski")

(deftest test-simple
  (are [result exp] (= result (run exp))
       "*" (.* i)
       "Hello" (.o (.l (.l (.e (.H i)))))))

(deftest test-defu
  (testing "s-expression"
   (are [result exp] (= result (run exp))
        "*" (print i)
        "*" (print 1)
        "**" (print (+ 1 1))
        "******" (print (+ 2 (* 2 2)))))
  (testing "string expression"
    (is (= "***" (run (print three-as-str))))))

(deftest test-anonymous-fns
  (are [result exp] (= result (run exp))
       "*" (print (fn [x] x))
       "**" (print (fn [f x] (f (f x))))))





