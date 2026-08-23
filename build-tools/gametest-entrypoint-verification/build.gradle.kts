/**
 * SPDX-AI-Disclosure: ai-assisted
 * SPDX-AI-Model: gpt-5-6
 * SPDX-AI-Provider: OpenAI
 * SPDX-AI-Scope: hand-written with AI guidance
 */

plugins {
    `kotlin-dsl`
    id("base")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.186")
}

gradlePlugin {
    plugins {
        register("gametestEntrypointVerification") {
            id = "gametest-entrypoint-verification"
            implementationClass = "io.github.thatdutchguy.minecraft_modding.gradle.gametest.GameTestEntrypointVerificationPlugin"
        }
    }
}
