(ns unlambda-clj.core)

(def cur-char (atom nil))

(defn evaluate
  ([program] (evaluate program identity))
  ([program result-callback]
     (letfn [(app [[func closure] arg cont]
;               (println (str "ARGS TO APP: " [[func closure] arg]))
               (case func
                 "." (do (.write *out* closure) #(cont arg))
                 "r" (do (.write *out* "\n") #(cont arg))
                 "i" #(cont arg)
                 "k" #(cont ["k1" arg])
                 "k1" #(cont closure)
                 "s" #(cont ["s1" arg])
                 "s1" #(cont ["s2" [closure, arg]])
                 "s2" (let [[f1 f2] closure] #(ev ["`" [["`" [f1 arg]] ["`" [f2 arg]]]] cont))
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

(defn parse [program]
  (let [p (atom program)]
    (letfn [(snext [cs] (apply str (next cs)))
            (parse-out []
              (condp re-find @p
                #"^`" (do (swap! p snext) (let [a (parse-out), b (parse-out)] ["`" [a b]]))
                #"^[rksivdce@|]" (let [r [(str (first @p)) nil]] (swap! p snext) r)
                #"^[.?]." (let [result [(str (first @p)) (str (fnext @p))]]
                            (swap! p #(apply str (nnext %)))
                            result)
                #"^(\s+|\#.*)" :>> (fn [ws] (do (swap! p #(apply str (nthnext % (count (ws 1)))))
                                                (parse-out)))))]
      (parse-out))))

(defn interpret [code] (evaluate (parse code)))
(defn -main [& args]
  
  (interpret "`r```si`k``s ``s`kk `si ``s``si`k ``s`k`s`k ``sk ``sr`k.* i r``si``si``si``si``si``si``si``si``si`k`ki"))
