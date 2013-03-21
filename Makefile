default: build
all: build

build:
	cljsc game.cljs \
			'{:optimizations :simple :pretty-print true}' \
			> ./game.js

develop:
	cljs-watch game.cljs \
		'{:optimizations :simple :pretty-print true :output-to "./game.js"}'
