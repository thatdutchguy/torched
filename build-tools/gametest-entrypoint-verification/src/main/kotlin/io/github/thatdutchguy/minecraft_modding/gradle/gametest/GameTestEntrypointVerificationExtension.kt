/**
 * SPDX-AI-Disclosure: ai-generated
 * SPDX-AI-Model: gpt-5-6
 * SPDX-AI-Provider: OpenAI
 * SPDX-AI-Scope: Initial version AI-generated.
 */

package io.github.thatdutchguy.minecraft_modding.gradle.gametest

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class GameTestEntrypointVerificationExtension {
    abstract val classDirectories: ConfigurableFileCollection
    abstract val fabricModJson: RegularFileProperty
    abstract val testPackage: Property<String>
}
