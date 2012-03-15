(ns unlambda-clj.core
  (:refer-clojure :exclude [read eval apply])
  (:use [clojure.main :only [repl]]))

(declare apply read) ; do i even really need to do this?

(defn- getc [in] (let [c (.read in)] (if (= -1 c) (throw (Exception. "Unexpected EOF!")) (char c))))

(def reader-map
  "A map of characters to the functions which parse them."
  (reduce (fn [acc ch] (assoc acc ch #(vector (keyword (str ch)))))
          {\@ #(vector :at), ; [:at]
           \r #(vector :.), ; [:.], short for [:. \newline]
           \? #(vector :? (getc *in*)), ; [:? x] for any characters x.
           \. #(vector :. (getc *in*)), ; [:. x] for any characters x
           \# #(do (.readLine *in*) (read)), ; discard rest of the line
           \` #(let [func (read), arg (read)] [:ap [func arg]])} ; function application
          (seq "ksivdce|"))) ; single character builtins that become to [(keyword (str char))]

(defn read "read a single unlambda expression from *in*"
  ([] (let [c (getc *in*)]
        (if-let [f (reader-map c)] (f)
                (if (Character/isWhitespace c) (recur)
                    (throw (Exception. (str "unknown input character: " c))))))))

(def cchar "The current character" (atom nil))

(defn eval
  "Evaluate a function, call cont with the result. "
  [[func scope :as f] cont]
  (if-not (= func :ap) (trampoline cont f)
          (let [[func arg] scope]
            (recur func (fn [eved-f]
                          (if (= :d (first eved-f)) #(cont [:d1 arg])
                              #(eval arg (fn [eved-a] (apply eved-f eved-a cont)))))))))

(defmulti apply
  "Applies a unlambda function (a vector of the name and,
   for lack of a better term, scope) to its argument. Typically
   calls cont with the result (but that depends on the function)"
  (fn [func arg cont] (func 0)))

(defmethod apply :default [[f] & _] ; default, error out.
  (throw (IllegalArgumentException. (str "unknown function: " f))))

(defmethod apply :.  [[f ch] a cc] ; print ch or \n
  (do (.write *out* (int (or ch \newline))) #(cc a)))

(defmethod apply :i  [f a cc] #(cc a)) ; identity
(defmethod apply :v  [f a cc] #(cc [:v])) ; ignore arg, return :v

(defmethod apply :k  [f a cc] #(cc [:k1 a])) ; curry to :k1
(defmethod apply :k1 [[f s] a cc] #(cc s)) ; (fn [x y] x)

(defmethod apply :s  [f a cc] #(cc [:s1 a])) ; curry to :s1
(defmethod apply :s1 [[f s] a cc] #(cc [:s2 [s a]])) ; curry to :s2
(defmethod apply :s2 [[f [f1 f2]] a cc] ; s is equivalent to (fn [x y z] ((x z) (y z)))
  #(eval [:ap [[:ap [f1 a]] [:ap [f2 a]]]] cc))

(defmethod apply :d1 [[f s] a cc]
  #(eval [:ap [s a]] cc)) ; called delayed function on a

(defmethod apply :c  [f a cc] #(eval [:ap [a [:c1 cc]]] cc)); eval [:c1 cc]
(defmethod apply :c1 [[f s] a _] #(s a)) ; s is the current continuation

(defmethod apply :e  [f a cc] a) ; simply return the value to exit.

(defmethod apply :?  [[f ch] a cc] ; compare cchar to ch, return :i if they're the same, :v otherwise
  #(eval (if (= ch (char @cchar)) [:ap [a [:i]]] [:ap [a [:v]]]) cc))

;; if cchar is EOF or if cchar hasnt' been set yet, return :v, otherwise [:. @cchar]
(defmethod apply :|  [f a cc]
  #(eval (if (and @cchar (not (neg?  @cchar))) [:ap [a [:. @cchar]]] [:ap [a [:v]]]) cc))

(defmethod apply :at [f a cc] ; set cchar to character read from *in*. return :v if eof, :i otherwise
  (do (reset! cchar (.read *in*))
      #(eval [:ap [a [(if (= @cchar -1) :v :i)]]] cc)))

(defn read-repl [prompt exit]
  (let [c (.read *in*)] (if (neg? c) exit (do (.unread *in* c) (read)))))

(defn -main  [& args]
  (repl :eval #(eval % identity), :read read-repl, :prompt #(print "> "), :need-prompt #(identity true)))

