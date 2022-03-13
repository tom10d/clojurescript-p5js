# ClojureScript + p5.js Demo Collection

Collection of demos using ClojureScript and the JavaScript library
p5.js, with a hefty helping of shadow-cljs.

At the moment there're just two demos in here: a basic example that
puts some shapes on the screen and a snake game.

- https://clojurescript.org/
- https://github.com/thheller/shadow-cljs
- https://p5js.org/

## Run the demos

Clone the repository and run

    npm install

Specify which demo to run in the file src/main/core.cljs by editing
the ":require" line. Available demos:

- "main.basic" Draws some simple shapes.
- "main.snake" Snake game.

Run

    npx shadow-cljs watch app

Open http://localhost:3000/ in your favorite browser.

## What are .projectile and .dir-locals.el?

These files are only useful if you're using Emacs, if not you can
ignore them.

- .projectile is used by an Emacs package called "projectile", which is
helpful in managing "projects", and a git repo counts as a project.
- .dir-locals.el provides a way to customize Emacs on a per-directory
basis.

## License

Copyright (c) 2022 tom10d

Distributed under the MIT License.