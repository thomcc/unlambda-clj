(ns unlambda-clj.test.core
  (:use [unlambda-clj.core])
  (:use [clojure.test]))

(deftest test-parse-fn
  (is (= ["r" nil] (parse "r")))
  (is (= ["k" nil] (parse "   k ")))
  (is (= ["k" nil] (parse "kk"))))

(deftest test-parse-dotq
  (is (= ["." "a"] (parse ".a")))
  (is (= ["." "`"] (parse ".`")))
  (is (= ["?" "#"] (parse "?#")))
  (is (= ["." " "] (parse ". ")))
  (is (= ["?" "?"] (parse "??"))))


(deftest test-parse-app
  (is (= ["`" [["s" nil] ["k" nil]]] (parse "`sk")))
  (is (= ["`" [["`" [["i" nil] ["i" nil]]] ["i" nil]]] (parse "``iii"))))

(defn unlambdify
  "interpret an unlambda expression, return what would be printed as a string.
   optionally takes an input string"
  ([code] (unlambdify code ""))
  ([code in] (with-in-str in (with-out-str (interpret code)))))

(deftest test-eval
  "The tests from ftp://quatramaran.ens.fr/pub/madore/unlambda/tests/unlambda-tests"
  (testing "Simple eliminations"
    (testing "produces a *"
      (are [code] (= "*\n" (unlambdify code))
           "`r`.*i"
           "`r`d`.*i"
           "`r``d.*i "
           "`r``dd`.*i"
           "`r```sd.*i"
           "`r```s`kd`.*ii"))
    (testing "produces a blank line"
      (are [code] (= "\n" (unlambdify code))
           "`r```kdi`.*i"
           "`r``id`.*i")))
  (testing "abstraction elimination from `.*i"
    (testing "produces a *"
      (are [code] (= "*\n" (unlambdify code))
           "`r```s`k.*`kii"
           "`r``k`.*ii"
           "`r```si`ki.*"
           "`r```s`k.*ii")))
  (testing "abstraction elimination from `d`.*i"
    (testing "produces a blank line"
      (are [code] (= "\n" (unlambdify code))
           "`r```s`kd``s`k.*`kii"
           "`r``k`d`.*ii"
           "`r```si``s`k.*`kid"
           "`r```s`kd``sii.*"
           "`r```s`kd``s`k.*ii"
           "`r```s`kd.*i"))
    (testing "produces a *"
      (are [code] (= "*\n" (unlambdify code))
           "`r```s`kd`k`.*ii"
           "`r```si`k`.*id")))
    (testing "abstraction elimination from ``d.*i"
      (testing "produces a *"
        (are [code] (= "*\n" (unlambdify code))
             "`r```s``s`kd`k.*`kii"
             "`r```s`k`d.*`kii"
             "`r``k``d.*ii"
             "`r```s``si`k.*`kid"
             "`r```s``s`kdi`ki.*"
             "`r```sd`ki.*"
             "`r```s``s`kd`k.*ii"
             "`r```s`k`d.*ii")))
    (testing "abstraction elimination from ``id`.*i"
      (testing "produces a blank line"
        (are [code] (= "\n" (unlambdify code))
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
        (are [code] (= "*\n" (unlambdify code))
             "`r```s``s`ki`kd`k`.*ii"
             "`r```s`k`id`k`.*ii"
             "`r```s``si`kd`k`.*ii"
             "`r```si`k`.*id"
             "`r```s``s`kii`k`.*id")))
    (testing "abstraction elimination from ``dd`.*i"
      (testing "produces a *"
        (are [code] (= "*\n" (unlambdify code))
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
             "`r```s`k`dd``si`ki.*")))
    (testing "abstraction elimination from ```kdi`.*i"
    (testing "produces a blank line"
      (are [code] (= "\n" (unlambdify code))
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
      (are [code] (= "*\n" (unlambdify code))
           "`r```s``s``s`kk`kd`ki`k`.*ii"
           "`r```s``s`k`kd`ki`k`.*ii"
           "`r```s`k``kdi`k`.*ii"
           "`r```s``s``si`kd`ki`k`.*ik"
           "`r```s``s``s`kki`ki`k`.*id"
           "`r```s``sk`ki`k`.*id "
           "`r```s``s``s`kk`kdi`k`.*ii")))
    (testing "abstraction elimination from ```s`kd.*i"
      (testing "produces a blank line"
        (are [code] (= "\n" (unlambdify code))
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
             "`r```s`k``s`kd.*ii")))
    (testing "Miscellaneous eliminations"
      (testing "produces a blank line"
        "`r```s``s``s`ks``s`kk`kd``ssi`k.*i"
        "`r```s``s``s`ks``s`kk`kds`k.*i"
        "`r```s``s``s`ks`k`kd``ssi`k.*i"
        "`r```s``s``s`ks`k`kds`k.*i"
        "`r```s``s`k`s`kd``ssi`k.*i"
        "`r```s``s`k`s`kds`k.*i")))










