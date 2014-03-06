# clojure(script) stack

### IN PROGRESS - bugged

example stack using clojure and clojurescript

- [ring](https://github.com/ring-clojure/ring) for web server
- [ring-edn](https://github.com/tailrecursion/ring-edn) for [edn](https://github.com/edn-format/edn) middleware
- [compojure](https://github.com/weavejester/compojure) for web server routing
- [om](https://github.com/swannodette/om) for views (using react)
- [om-sync](https://github.com/swannodette/om-sync) for data sync over [edn](https://github.com/edn-format/edn)
- [korma](http://sqlkorma.com/) for sql queries
- [lobos](https://github.com/budu/lobos) for sql migrations

## how to install

install java JDK version 6 or later

```
# get lein (https://github.com/technomancy/leiningen)
wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -P ~/bin
chmod +x ~/bin/lein

# get this project
git clone https://github.com/holonomy/clojure-stack

# go to this project
cd clojure-stack

# get dependencies
lein deps
```

## how to run

```
# migrate database
lein lobos migrate

# compile clojurescript
lein cljsbuild once

# run server
lein run
```

## how to develop
```
# migrate database
lein lobos migrate

# in one terminal, compile clojurescript
lein cljsbuild auto dev

# in another terminal, run server
lein run
```
