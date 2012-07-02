(ns unlambda-clj.core
  (:refer-clojure :exclude [read eval apply])
  (:use [clojure.main :only [repl]]))

(declare apply read)

(defn- getc
  "Read a single character from `in`, throw an error if it's EOF."
  [in]
  (let [c (.read in)]
    (if (= -1 c)
      (throw
       (Exception. "Unexpected EOF!"))
      (char c))))

(def reader-map
  "A map of characters to the functions which parse them."
  (reduce (fn [acc ch]
            (assoc acc ch
                   #(vector (keyword (str ch)))))
          {;; [:at]
           \@ #(vector :at)
           ;; [:.], short for [:. \newline]
           \r #(vector :.)
           ;; [:? x] for any characters x.
           \? #(vector :? (getc *in*))
           ;; [:. x] for any characters x
           \. #(vector :. (getc *in*))
           ;; discard rest of the line
           \# #(do (.readLine *in*) (read))
           ;; function application
           \` #(let [func (read), arg (read)]
                 [:ap [func arg]])}
           ;; single character builtins that become to [(keyword (str char))]
          (seq "ksivdce|")))

(defn read
  "read a single unlambda expression from *in*"
  []
  (let [c (getc *in*)]
    (if-let [f (reader-map c)]
      (f)
      (if (Character/isWhitespace c)
        (recur)
        (throw
         (Exception.
          (str "unknown input character: " c)))))))

(def cchar "The current character" (atom nil))

(defn eval
  "Evaluate a function, call cont with the result. "
  [[func scope :as f] cont]
  (if-not (= func :ap)
    #(cont f)
    (let [[func arg] scope]
      (recur
       func
       (fn [eved-f]
         (if (= :d (first eved-f))
           #(cont [:d1 arg])
           #(eval arg
                  (fn [eved-a]
                    (apply
                     eved-f
                     eved-a
                     cont)))))))))

(defmulti apply
  "Applies a unlambda function (a vector of the name and,
   for lack of a better term, scope) to its argument. Typically
   calls cont with the result (but that depends on the function)"
  (fn [func arg cont]
    (func 0)))

(defmethod apply :default
  ;; default, error out.
  [[f] & _]
  (throw (IllegalArgumentException.
          (str "unknown function: " f))))

(defmethod apply :.
  ;; print ch or \n
  [[f ch] a cc]
  (do (.write *out* (int (or ch \newline)))
      #(cc a)))

(defmethod apply :i
  ;; identity
  [f a cc]
  #(cc a))
(defmethod apply :v
  ;; ignore arg, return :v
  [f a cc]
  #(cc [:v]))

(defmethod apply :k
  ;; curry to :k1
  [f a cc]
  #(cc [:k1 a]))

(defmethod apply :k1
  ;; (fn [x y] x)
  [[f s] a cc]
  #(cc s))

(defmethod apply :s
  ;; curry to :s1
  [f a cc]
  #(cc [:s1 a]))

(defmethod apply :s1
  ;; curry to :s2
  [[f s] a cc]
  #(cc [:s2 [s a]]))

(defmethod apply :s2
  ;; s is equivalent to (fn [x y z] ((x z) (y z)))
  [[f [f1 f2]] a cc]
  #(eval [:ap [[:ap [f1 a]] [:ap [f2 a]]]] cc))

(defmethod apply :d1
  ;; called delayed function on a
  [[f s] a cc]
  #(eval [:ap [s a]] cc))

(defmethod apply :c
  ;; eval [:c1 cc]
  [f a cc]
  #(eval [:ap [a [:c1 cc]]] cc))

(defmethod apply :c1
  ;; s is the current continuation
  [[f s] a _]
  #(s a))

(defmethod apply :e
  ;; simply return the value to exit.
  [f a cc]
  a)

(defmethod apply :?
  ;; compare cchar to ch, return :i if they're the same, :v otherwise
  [[f ch] a cc]
  #(eval (if (= ch (char @cchar))
           [:ap [a [:i]]]
           [:ap [a [:v]]])
         cc))


(defmethod apply :|
  ;; if cchar is EOF or if cchar hasnt' been set yet, return :v
  ;; otherwise [:. @cchar]
  [f a cc]
  #(eval
    (if (and @cchar (not (neg?  @cchar)))
      [:ap [a [:. @cchar]]]
      [:ap [a [:v]]])
    cc))

(defmethod apply :at
  ;; set cchar to character read from *in*. return :v if eof, :i otherwise
  [f a cc]
  (do (reset! cchar (.read *in*))
      #(eval [:ap [a [(if (= @cchar -1) :v :i)]]]
             cc)))

(defn read-repl
  "Read a string from *in* for the repl."
  [prompt exit]
  (let [c (.read *in*)]
    (if (neg? c)
      exit
      (do (.unread *in* c)
          (read)))))


(defn repl-eval
  "Evaluate a single piece of parsed unlambda code, and return the result."
  [form]
  (trampoline eval form identity))

(defn -main  [& args]
  (repl :eval repl-eval
        :read read-repl
        :prompt #(print "> "),
        :need-prompt #(identity true)))
