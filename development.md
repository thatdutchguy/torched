# Development 

This mod has been built using the [Fabric modding API](https://fabricmc.net/).

## Directory structure

- `src/main`: Server-side and common modules
- `src/client`: Client-side modules
- `src/test`: Unit tests
- `src/gametest`: Integration and E2E tests
- `build-tools/gametest-entrypoint-verification`: Gradle plugin for checking
gametest configuration

## Common gradle tasks

- Clean:

`./gradlew clean :gametest-entrypoint-verification:clean`

- Build and run unit tests + server-side GameTests

`./gradlew build`

- Unit tests:

`./gradlew test`

- Server-side integration tests:

`./gradlew runGameTest`

- E2E test:

`./gradlew runClientGameTest`

## Resources and references

### Tools

- [Fabric Template generator](https://fabricmc.net/develop/template/): Used to generate the initial mod structure.

### Docs

- [Fabric Developer Guides](https://docs.fabricmc.net/develop/)
- [Fabric API Javadoc](https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/)
- [Fabric Yarn Javadoc](https://maven.fabricmc.net/docs/yarn-1.21.11+build.6/): Minecraft API bindings
- [LambDynamicLights documentation](https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/) 

### Source code

- [Mod Menu](https://github.com/TerraformersMC/ModMenu)
- [LambDynamicLights](https://github.com/LambdAurora/LambDynamicLights)
