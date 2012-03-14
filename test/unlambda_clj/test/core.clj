(ns unlambda-clj.test.core
  (:refer-clojure :rename {read core-read, eval core-eval, apply core-apply})
;  (:refer-clojure :exclude [read eval])
  (:use [unlambda-clj.core])
  (:use [clojure.test]))

(defn read-str [s] (with-in-str s (read)))

(defn interpret-str [s] (eval (read-str s) identity))

(defn u
  "interpret an unlambda expression as a string (w/ optional input), return the result of the expr"
  ([code] (u code ""))
  ([code s] (with-in-str s (interpret-str code))))

(defn us
  "interpret an unlambda expression, return what would be printed as a string.
   optionally takes an input string"
  ([code] (us code ""))
  ([code in] (with-out-str (with-in-str in (interpret-str code)))))

(deftest test-read-mappings
  (is (= [:.] (read-str "r")))
  (is (= [:k] (read-str "   k ")))
  (is (= [:k] (read-str "kk"))))

(deftest test-read-2vecs
  (is (= [:. \a] (read-str ".a")))
  (is (= [:. \`] (read-str ".`")))
  (is (= [:? \#] (read-str "?#")))
  (is (= [:. \space] (read-str ". ")))
  (is (= [:? \?] (read-str "??"))))


(deftest test-read-app
  (is (= [:ap [[:s] [:k]]] (read-str "`sk")))
  (is (= [:ap [[:ap [[:i] [:i]]] [:i]]] (read-str "``iii"))))

(deftest test-syms
  (is (= [:at] (read-str "@")))
  (is (= [:|] (read-str "|")))
  (is (= [:. \*] (read-str ".*")))
  (is (= [:? \*] (read-str "?*"))))

(deftest test-chars
  (doseq [c (seq "ksivdce|")]
    (is (= [(keyword (str c))]
           (read-str (str c))))))

(deftest test-comments
  (is (= [:ap [[:s] [:k]]]
         (read-str "# this is a comment!
`      # more comments
# i am diligently commenting my code
s
# only whitespace on next line
                                                                      
# okay lets try to finish this up.
k # comments rule!"))))
;; the following tests are from
;; ftp://quatramaran.ens.fr/pub/madore/unlambda/tests/unlambda-tests
;; excessively tests r, .x, d, s, k, and i.
(deftest test-simple-elims
  (testing "produces a *"
    (are [code] (= "*\n" (us code))
         "`r`.*i"
         "`r``d.*i "
         "`r``dd`.*i"
         "`r```sd.*i"
         "`r```s`kd`.*ii"))
  (testing "produces a blank line"
    (are [code] (= "\n" (us code))
         "`r`d`.*i"
         "`r```kdi`.*i"
         "`r``id`.*i")))

(deftest test-elims1
  (testing "abstraction elimination from `.*i"
    (testing "produces a *"
      (are [code] (= "*\n" (us code))
           "`r```s`k.*`kii"
           "`r``k`.*ii"
           "`r```si`ki.*"
           "`r```s`k.*ii"))))

(deftest test-elims2
  (testing "abstraction elimination from `d`.*i"
    (testing "produces a blank line"
      (are [code] (= "\n" (us code))
           "`r```s`kd``s`k.*`kii"
           "`r``k`d`.*ii"
           "`r```si``s`k.*`kid"
           "`r```s`kd``sii.*"
           "`r```s`kd``s`k.*ii"
           "`r```s`kd.*i"))
    (testing "produces a *"
      (are [code] (= "*\n" (us code))
           "`r```s`kd`k`.*ii"
           "`r```si`k`.*id"))))
(deftest test-elims3
  (testing "abstraction elimination from ``d.*i"
    (testing "produces a *"
      (are [code] (= "*\n" (us code))
           "`r```s``s`kd`k.*`kii"
           "`r```s`k`d.*`kii"
           "`r``k``d.*ii"
           "`r```s``si`k.*`kid"
           "`r```s``s`kdi`ki.*"
           "`r```sd`ki.*"
           "`r```s``s`kd`k.*ii"
           "`r```s`k`d.*ii"))))
(deftest test-elims4
   (testing "abstraction elimination from ``id`.*i"
      (testing "produces a blank line"
        (are [code] (= "\n" (us code))
             "`r```s`k`id``s`k.*`kii"
             "`r```s``s`ki`kd``s`k.*`kii"
             "`r``k``id`.*ii"
             "`r```s``si`kd``s`k.*`kii"
             "`r```s``s`ki`kd``s`k.*ii"
             "`r```s`k`id``s`k.*ii"
             "`r```s``s`ki`kd.*i"
             "`r```s`k`id.*i"
             "`r```s``si`kd``s`k.*ii"
             "`r```s``si`kd.*i"
             "`r```s``s`kii``s`k.*`kid"
             "`r```si``s`k.*`kid"
             "`r```s``s`ki`kd``si`ki.*"
             "`r```s`k`id``si`ki.*"))
      (testing "produces a *"
        (are [code] (= "*\n" (us code))
             "`r```s``s`ki`kd`k`.*ii"
             "`r```s`k`id`k`.*ii"
             "`r```s``si`kd`k`.*ii"
             "`r```si`k`.*id"
             "`r```s``s`kii`k`.*id"))))
(deftest test-elims5
  (testing "abstraction elimination from ``dd`.*i"
    (testing "produces a *"
      (are [code] (= "*\n" (us code))
           "`r```s``s`kd`kd``s`k.*`kii"
           "`r```s``s`kd`kd`k`.*ii"
           "`r```s`k`dd``s`k.*`kii"
           "`r```s`k`dd`k`.*ii"
           "`r``k``dd`.*ii"
           "`r```s``s`kd`kd``s`k.*ii"
           "`r```s`k`dd``s`k.*ii"
           "`r```s``s`kd`kd.*i"
           "`r```s`k`dd.*i"
           "`r```s``s`kdi``s`k.*`kid"
           "`r```s``s`kdi`k`.*id"
           "`r```sd``s`k.*`kid"
           "`r```sd`k`.*id"
           "`r```s``si`kd``s`k.*`kid"
           "`r```s``si`kd`k`.*id"
           "`r```s``sii``s`k.*`kid"
           "`r```s``sii`k`.*id"
           "`r```s``s`kd`kd``si`ki.*"
           "`r```s`k`dd``si`ki.*"))))
(deftest test-elims6
  (testing "abstraction elimination from ```kdi`.*i"
    (testing "produces a blank line"
      (are [code] (= "\n" (us code))
           "`r```s``s``s`kk`kd`ki``s`k.*`kii"
           "`r```s``s`k`kd`ki``s`k.*`kii"
           "`r```s`k``kdi``s`k.*`kii"
           "`r```s``s``si`kd`ki``s`k.*`kik"
           "`r```s``s``s`kki`ki``s`k.*`kid"
           "`r``k```kdi`.*ii"
           "`r```s``sk`ki``s`k.*`kid"
           "`r```s``s``s`kk`kdi``s`k.*`kii"
           "`r```s``s`k`kdi``s`k.*`kii"
           "`r```s`kd``s`k.*`kii"
           "`r```s``s``s`kk`kd`ki``s`k.*ii"
           "`r```s``s`k`kd`ki``s`k.*ii"
           "`r```s`k``kdi``s`k.*ii"
           "`r```s``s``s`kk`kdi.*i"
           "`r```s``s`k`kd`ki.*i"
           "`r```s`k``kdi.*i"
           "`r```s``s``s`kk`kdi``s`k.*ii"
           "`r```s``s`k`kdi``s`k.*ii"
           "`r```s``s``s`kk`kd`ki``si`ki.*"
           "`r```s``s`k`kd`ki``si`ki.*"
           "`r```s`k``kdi``si`ki.*"))
    (testing "produces a *"
      (are [code] (= "*\n" (us code))
           "`r```s``s``s`kk`kd`ki`k`.*ii"
           "`r```s``s`k`kd`ki`k`.*ii"
           "`r```s`k``kdi`k`.*ii"
           "`r```s``s``si`kd`ki`k`.*ik"
           "`r```s``s``s`kki`ki`k`.*id"
           "`r```s``sk`ki`k`.*id "
           "`r```s``s``s`kk`kdi`k`.*ii"))))
(deftest test-elims7
  (testing "abstraction elimination from ```s`kd.*i"
    (testing "produces a blank line"
      (are [code] (= "\n" (us code))
           "`r```s``s``s`ks``s`kk`kd`k.*`kii"
           "`r```s``s``s`ks`k`kd`k.*`kii"
           "`r```s``s`k`s`kd`k.*`kii"
           "`r```s`k``s`kd.*`kii"
           "`r``k```s`kd.*ii"
           "`r```s``s``si``s`kk`kd`k.*`kis"
           "`r```s``s``si`k`kd`k.*`kis"
           "`r```s``s``s`ks``si`kd`k.*`kik"
           "`r```s``s``s`ks``s`kki`k.*`kid"
           "`r```s``s``s`ksk`k.*`kid"
           "`r```s``s``s`ks``s`kk`kdi`ki.*"
           "`r```s``s``s`ks`k`kdi`ki.*"
           "`r```s``s`k`s`kdi`ki.*"
           "`r```s`s`kd`ki.*"
           "`r```s``s``s`ks``s`kk`kd`k.*ii"
           "`r```s``s``s`ks`k`kd`k.*ii"
           "`r```s``s`k`s`kd`k.*ii"
           "`r```s`k``s`kd.*ii"))))
(deftest test-misc-elims
  (testing "Miscellaneous eliminations"
    (testing "produces a blank line"
      "`r```s``s``s`ks``s`kk`kd``ssi`k.*i"
      "`r```s``s``s`ks``s`kk`kds`k.*i"
      "`r```s``s``s`ks`k`kd``ssi`k.*i"
      "`r```s``s``s`ks`k`kds`k.*i"
      "`r```s``s`k`s`kd``ssi`k.*i"
      "`r```s``s`k`s`kds`k.*i")))

(deftest test-squares "neato example program from CUAN"
  (is (= "\n\n\n*\n\n**\n**\n\n***\n***\n***\n\n****\n****\n****\n****\n\n*****\n*****\n*****\n*****\n*****\n\n******\n******\n******\n******\n******\n******\n\n*******\n*******\n*******\n*******\n*******\n*******\n*******\n\n********\n********\n********\n********\n********\n********\n********\n********\n"
         (us "`r```si`k``s ``s`kk `si ``s``si`k ``s`k`s`k ``sk ``sr`k.* i r``si``si``si``si``si``si``si``si``si`k`ki"))))

(deftest test-input (is (= "abc" (us "```s`d`@|i`ci" "abc"))))

(deftest test-void (is (= [:v] (u "````````````v.arsidcevk?x@|"))))

(deftest test-iden (is (= [:.] (u "`ir"))))

(deftest test-k
  (is (= [:k1 [:. \a]] (u "`k.a")))
  (is (= [:v] (u "``kvi"))))

(deftest test-s
  (is (= [:s1 [:. \a]] (u "`s.a.b.c")))
  (is (= [:s2 [[:. \a] [:. \b]]] (u "``s.a.b.c")))
  (is (= [:. \c] (u "```s.a.b.c"))))

(deftest test-e
  (is (= [:. \a] (u "``e.ai"))))

(deftest test-c
  (is (= :c1 ((u "``.*`ci`.@`ce") 0))))

(deftest test-d ; ensure proper implementation of d
  (is (= [:d1 [:ap [[:.] [:i]]]] (u "```s`kdri"))))

(deftest test-church-nums-and-printing
  (is (= "foop foop foop "
         (us "```si`k``s.f``s.o``s.o``s.p``s. i``si``si``si`ki"))))

(deftest test-quine
  (let [src "``d.v```s``si`kv``si`k`d`..`.c`.s`.``.``.s`.``.`v``s``sc.```s``sc.```s``sc.d``s``sc..``s``sc.v``s``sc.```s``sc.```s``sc.```s``sc.s``s``sc.```s``sc.```s``sc.s``s``sc.i``s``sc.```s``sc.k``s``sc.v``s``sc.```s``sc.```s``sc.s``s``sc.i``s``sc.```s``sc.k``s``sc.```s``sc.d``s``sc.```s``sc..``s``sc..``s``sc.```s``sc..``s``sc.c``s``sc.```s``sc..``s``sc.s``s``sc.```s``sc..``s``sc.```s``sc.```s``sc..``s``sc.```s``sc.```s``sc..``s``sc.s``s``sc.```s``sc..``s``sc.```s``sc.```s``sc..``s``sc.```s``sc.vv"]
    (is (= src (us src)))))

