{
  description = "Torched Minecraft mod development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jdk25 = pkgs.jdk25;
      in {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk25
            pkgs.kotlin
          ];

          shellHook = ''
            export JAVA_HOME="${jdk25.home}"
            echo "Java: $(java -version 2>&1 | head -n 1)"
            echo "Kotlin: $(kotlin -version)"
          '';
        };
      }
    );
}
