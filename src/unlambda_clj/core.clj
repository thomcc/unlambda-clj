(ns unlambda-clj.core
  (:require [clojure.main :as main]))

(def cur-char (atom nil))

(defn evaluate
  ([program] (evaluate program identity))
  ([program result-callback]
     (letfn [(app [[func closure] arg cont]
;               (println (str "ARGS TO APP: " [[func closure] arg]))
               (case func
                 "." (do (.write *out* (int closure)) #(cont arg))
                 "r" (do (.write *out* (int \newline)) #(cont arg))
                 "i" #(cont arg)
                 "k" #(cont ["k1" arg])
                 "k1" #(cont closure)
                 "s" #(cont ["s1" arg])
                 "s1" #(cont ["s2" [closure, arg]])
                 "s2" (let [[f1 f2] closure]
                        #(ev ["`" [["`" [f1 arg]] ["`" [f2 arg]]]] cont))
                 "v" #(cont ["v", nil])
                 "d1" #(ev ["`" [closure, arg]] cont)
                 "e" (result-callback arg)
                 "@" (do (reset! cur-char (.read *in*))
                         #(ev ["`" [arg [(if (= @cur-char -1) "v" "i") nil]]] cont))
                 "|" #(ev ["`" [arg (if (= -1 @cur-char) ["v" nil] ["." @cur-char])]] cont)
                 "?" #(ev ["`" [arg [(if (= @cur-char closure) "i" "v") nil]]] cont)
                 "c" #(ev ["`" [arg ["c1" cont]]] cont)
                 "c1" #(closure arg)))
             (ev [[func closure] cont]
;               (println (str "ARGS TO EV: " [func closure]))
               (if-not (= func "`")
                 (trampoline cont [func closure])
                 (let [[func arg] closure]
                   (recur func
                     (fn [op]
                       (if (= "d" (first op))
                         #(cont ["d1" arg])
                         #(ev arg (fn [earg]
                                    (app op earg cont)))))))))]
       (ev program result-callback))))


(defn- getc [in]
  (let [c (.read in)]
    (if (neg? c) (throw (Exception. "Unexpected EOF!")) (char c))))

(defn parse-in []
  (let [c (getc *in*)]
    (cond (= c \`) (let [a (parse-in), b (parse-in)] ["`" [a b]])
          (re-find #"[rksivdce@|]" (str c)) [(str c) nil]
          (or (= c \.) (= c \?)) [(str c) (getc *in*)]
          (Character/isWhitespace c) (recur)
          (= \# c) (.readLine *in*)
          :else (throw (Exception. (str "unknown input character: " c))))))

(defn parse [s] (with-in-str s (parse-in)))

(defn interpret [code] (evaluate (parse code)))

(defn parse-repl [prompt exit]
  (let [c (.read *in*)]
    (cond (neg? c) exit,
          (= (char c) \newline) prompt
          :else (do (.unread *in* c) (parse-in)))))

(defn -main  [& args] (main/repl :eval evaluate, :read parse-repl, :prompt #(print "> ")))

