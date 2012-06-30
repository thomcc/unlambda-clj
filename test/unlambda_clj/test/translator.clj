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
(defu dec [n f x] (n (fn [g h] (h (g f))) (fn [u] x) i))

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

  (testing "fn call in the first position of expression"
    (is (= "**" (run ((2 .*) i))))))

(deftest test-string-expressions
  (are [result exp] (= result (run exp))
       "***" (print three-as-str)
       "**" (print (+ "i" "i"))))

(deftest test-anonymous-fns
  (are [result exp] (= result (run exp))
       "*" (print (fn [x] x))
       "**" (print (fn [f x] (f (f x))))
       "***" (print (dec (* 2 2)))))






