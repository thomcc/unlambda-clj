# Unlambda-clj

Unlambda is a Clojure implementation of David Madore's brilliant esoteric
language, unlambda. It implements the "unlambda 2.0 standard", and passes every
test that I've been able to find on the internet or come up with myself!

## Usage

Running main (by way of `lein trampoline run`) will start a repl, you can tell everything is working by entering

    ```si`k``s.C``s.l``s.o``s.j``s.u``s.r``s.e
    ``s. ``s.r``s.o``s.c``s.k``s.s``s.!``sri``
    si``si``si``si``si``si``si``si``si``si`ki

Which should print "Clojure rocks!" ten times.

## What is Unlambda?

Unlambda is an esoteric programming language.  Unlike the typical Turing-machine
based esoteric languages, unlambda is based on the untyped lambda
calculus. 

If you're familiar with functional programming, it shouldn't surprise you that
the untyped lambda calculus is Turing-complete, however it may surprise that
*you don't even need lambda*.  Unlambda, in fact, does away with lambda
abstraction altogether, and instead uses the S, K, and I combinators to achieve
Turing-completeness (really just S and K).

## More about Unlambda

Unlambda functions are applied to each other with the `` ` `` (back-quote)
character.  Every function takes exactly one argument (multiple arguments can be
simulated via currying), meaning that this notation is unambiguous. Unlambda has
the following built in functions:

* `k`: The K combinator. Equivalent to the following Clojure code: `(fn [x] (fn
  [y] x))`. That is, ``` ``k<x><y> ``` evaluates to `<x>`. 
* `s`: The S combinator. Equivalent to the following Clojure code: `(fn [x] (fn
  [y] (fn [z] ((x z) (y z))))`.  That is, ```` ```s<x><y><z> ```` evaluates to
  ``` ``<x><z>`<y><z> ```.
* `i`: The I combinator.  Returns its argument (identity). The same as ``` ``skk
  ```.
* `v`: Void.  Discards its argument and returns `v`.
* `c`: Call-with-current-continuation.  Applies its argument to the current
  continuation (e.g. `` `c<x> `` evaluates to `` `<x><current-continuation> ``). More
  information on what this means is can be found in various places on the
  internet.
* `d`: Delay (special form). Delays the evaluation of its argument `<f>` until it is
  forced by being applied to another argument `<a>`.  When that occurs, `<a>` will
  be evaluated before `<f>`, and then `<f>` will be applied to `<a>`.
* `.x`: Write. For all characters `x`, `.x` is a function which prints out the
  character `x`, and returns its argument.
* `r`: Shorthand for `.<literal newline>`. That is, `r` prints a newline, and
  returns its argument.
* `e`: Exit. Halts evaluation, returning its argument as the result of the
  program.
* `@`: Read: Reads a single character.  Sets the "current character" to the
  value of the character read in (from `*in*`).
* `?x`: Compare Read: Similar to `.x`, `?x` is defined for all characters `x`.
  It compares `x` to the "current character".  If they have the same
  value, it returns `i`, otherwise `v`.
* `|`: Print Current Character: If a value for the "current character" exists,
  then `` `|<x> `` evaluates to `` `<x>.<current character> ``. If the current
  character has not been set yet, then `` `|<x> `` evaluates to `` `<x>v ``. 

Additionally, this implementation discards all white-space, and treats `#` as a
comment character.  

For even more information, including how to actually go about writing unlambda
programs, consult <http://www.madore.org/~david/programs/unlambda/>


```
     ``.*`ci`.@`ci
```

## License

Copyright (C) 2012 Thom Chiovoloni

Distributed under the Eclipse Public License, the same as Clojure.
