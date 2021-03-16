{ pkgs ? import <nixpkgs> { } }:
with pkgs;
mkShell { buildInputs = [ nixpkgs-fmt leiningen clojure nodejs clojure-lsp ]; }

# installation:
# - npm install
# - lein something
