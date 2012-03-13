# Unlambda: The Esoteric Language of the Future, Today!

Unlambda-clj is an implementation of David Madore's brilliant language:
`unlambda`.

Unlambda is an esoteric programming language.  Unlike the typical turing-machine
based esoteric languages, unlambda is based on the untyped lambda calculus
(specifically the calculus of SKI combinators).

Unlambda functions are applied to each other with the `\`` (back-quote)
character.  Every function takes exactly one argument (multiple arguments can be
simulated via currying), so this is unambiguous. Unlambda has the following
built in functions: 

* `k`: The K combinator. `\`\`k<x><y>` evaluates to `<x>`
* `s`: The S combinator. `\`\`\`s<x><y><z>` evaluates to `\`\`<x><z>\`<y><z>`
  (that is, `(s <x> <y> <z>)` evaluates to `((<x> <z>) (<y> <z>))`)
* `i`: The I combinator.  Returns its argument (identity).
* `v`: Void.  Discards its argument and returns `v`.
* `c`: Call-with-current-continuation.  Applies its argument to the current
  continuation (e.g. `\`c<x>` evaluates to `\`<x><current-continuation>`). More
  information on what this means is can be found in various places on the
  internet.
* `d`: Delay (special form). `\`d<x>` leaves `<x>` unevaluated (`d` delays the
  evaluation), however `\`\`d<x><y>` will evaluate to `\`<x><y>`.  In this
  second case, note that `<y>` will be evaluated _before_ `<x>`.
* `.x`: Write. For all characters `x`, `.x` is a function which prints out the character `x`.
* `r`: Shorthand for `.<newline>`. That is, `r` prints a newline.
* `e`: Exit. Halts evaluation, returning its argument as the result of the
  program.
* `@`: Read: Reads a single character.  Sets the "current character" to the
  value of the character read in (from `*in*`).
* `?x`: Compare Read: Similar to `.x`, `?x` is defined for all characters `x`.
  It compares `x` to the "current character".  If they have the same
  value, it returns `i`, otherwise `v`.
* `|`: Print Current Character: If a value for the "current character" exists,
  then `\`|<x>` evaluates to `\`<x>.<current character>`. If the current
  character has not been set yet, then `\`|<x>` evaluates to `\`<x>v`. 


## License

Copyright (C) 2012 Thom Chiovoloni

Distributed under the Eclipse Public License, the same as Clojure.
