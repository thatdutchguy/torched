# gametest-entrypoint-verification

After writing a [Fabric GameTest](https://docs.fabricmc.net/develop/automatic-testing#game-tests),
I would often forget adding it to `src/gametest/resources/fabric.mod.json`
resulting in the test not getting picked up.

This plugin will look at the configured test classes in the json file and
compare them to the actual classes, and will fail the build if there is a
mismatch.

## Example configuration:

build.gradle.kts

```kts
gameTestEntrypointVerification {
    classDirectories.from(
        sourceSets.named("gametest").map { it.output.classesDirs }
    )
    fabricModJson.set(
        layout.projectDirectory.file(
            "src/gametest/resources/fabric.mod.json"
        )
    )
    testPackage.set("io.github.thatdutchguy.torchedmetest")
}
```

## Running

The task runs automatically as part of the main `check`, `runGameTest`, and
`runClientGameTest` tasks.

You can also run it separately using the `verifyGameTestEntrypoints` task.

## AI Disclosure

The code in this module is ai-assisted, human-reviewed and customized.
I had no desire to write a Gradle plugin, though learned a lot about it in the
process.
