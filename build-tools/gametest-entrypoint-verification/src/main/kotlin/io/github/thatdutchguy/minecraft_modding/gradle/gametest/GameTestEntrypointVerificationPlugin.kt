/**
 * SPDX-AI-Disclosure: ai-assisted
 * SPDX-AI-Model: gpt-5-6
 * SPDX-AI-Provider: OpenAI
 * SPDX-AI-Scope: Initial version AI-generated; updated by hand.
 */

package io.github.thatdutchguy.minecraft_modding.gradle.gametest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class GameTestEntrypointVerificationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<GameTestEntrypointVerificationExtension>(
            "gameTestEntrypointVerification"
        )

        val verificationTask = project.tasks.register<VerifyGameTestEntrypoints>("verifyGameTestEntrypoints") {
            dependsOn("gametestClasses")
            classDirectories.from(extension.classDirectories)
            fabricModJson.convention(extension.fabricModJson)
            testPackage.convention(extension.testPackage)
        }

        val taskHooks = listOf("check", "runGameTest", "runClientGameTest")
        for (taskName in taskHooks) {
            project.rootProject.tasks.matching { it.name == taskName }.configureEach {
                dependsOn(verificationTask)
            }
        }
    }
}
