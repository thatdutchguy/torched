/**
 * SPDX-AI-Disclosure: ai-assisted
 * SPDX-AI-Model: gpt-5-6
 * SPDX-AI-Provider: OpenAI
 * SPDX-AI-Scope: Initial version AI-generated; updated by hand.
 */

package io.github.thatdutchguy.minecraft_modding.gradle.gametest

import groovy.json.JsonSlurper
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyGameTestEntrypoints : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classDirectories: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fabricModJson: RegularFileProperty

    @get:Input
    abstract val testPackage: Property<String>

    init {
        group = "verification"
        description = "Verifies that GameTest classes match the entrypoints in fabric.mod.json."
    }

    @TaskAction
    fun verify() {
        val discovered = discoverEntrypoints()
        val declared = readDeclaredEntrypoints()
        val problems = compare(discovered, declared)

        if (problems.isNotEmpty()) {
            throw GradleException(problems.format())
        }

        logger.lifecycle(
            "Verified {} server and {} client GameTest entrypoints.",
            discovered.server.size,
            discovered.client.size,
        )
    }

    private fun discoverEntrypoints(): Entrypoints {
        val packageName = testPackage.get().trim().removeSuffix(".")
        if (packageName.isEmpty()) {
            throw GradleException("testPackage must not be empty")
        }

        val classpath = classDirectories.files.filter { it.exists() }
        if (classpath.isEmpty()) {
            throw GradleException("No compiled GameTest class directories were found")
        }

        ClassGraph().overrideClasspath(classpath).acceptPackages(packageName).enableClassInfo().enableMethodInfo()
            .enableAnnotationInfo().enableExternalClasses().ignoreClassVisibility().ignoreMethodVisibility().scan()
            .use { scanResult ->
                val server =
                    scanResult.getClassesWithMethodAnnotation(SERVER_GAME_TEST_ANNOTATION).flatMap { annotatedClass ->
                            sequenceOf(annotatedClass) + annotatedClass.subclasses.asSequence()
                        }.filter { it.isConcreteTestClassIn(packageName) }.mapTo(sortedSetOf()) { it.name }

                val client = scanResult.getClassesImplementing(CLIENT_GAME_TEST_INTERFACE).asSequence()
                    .filter { it.isConcreteTestClassIn(packageName) }.mapTo(sortedSetOf()) { it.name }

                return Entrypoints(server = server, client = client)
            }
    }

    private fun readDeclaredEntrypoints(): Entrypoints {
        val jsonFile = fabricModJson.get().asFile
        val document = JsonSlurper().parse(jsonFile) as? Map<*, *>
            ?: throw GradleException("${jsonFile.path} must contain a JSON object")
        val entrypoints = document["entrypoints"] as? Map<*, *>
            ?: throw GradleException("${jsonFile.path} must contain an 'entrypoints' object")

        return Entrypoints(
            server = entrypoints.readEntrypoints(SERVER_ENTRYPOINT_KEY, jsonFile.path),
            client = entrypoints.readEntrypoints(CLIENT_ENTRYPOINT_KEY, jsonFile.path),
        )
    }

    private fun compare(discovered: Entrypoints, declared: Entrypoints): VerificationProblems {
        val declaredUnderBothKeys = declared.server intersect declared.client
        val serverDeclaredAsClient = (discovered.server intersect declared.client) - declaredUnderBothKeys
        val clientDeclaredAsServer = (discovered.client intersect declared.server) - declaredUnderBothKeys

        return VerificationProblems(
            missingServer = discovered.server - declared.server - declared.client,
            staleServer = declared.server - discovered.server - discovered.client,
            missingClient = discovered.client - declared.client - declared.server,
            staleClient = declared.client - discovered.client - discovered.server,
            serverDeclaredAsClient = serverDeclaredAsClient,
            clientDeclaredAsServer = clientDeclaredAsServer,
            declaredUnderBothKeys = declaredUnderBothKeys,
        )
    }
}

private fun ClassInfo.isConcreteTestClassIn(packageName: String): Boolean =
    !isAbstract && !isInterface && !isAnnotation && !isSynthetic && (name.startsWith("$packageName.") || name == packageName)

private fun Map<*, *>.readEntrypoints(key: String, filePath: String): Set<String> {
    val rawEntries = this[key] ?: return emptySet()
    val entries = rawEntries as? List<*> ?: throw GradleException("'$key' in $filePath must be an array")

    val values = entries.mapIndexed { index, entry ->
        when (entry) {
            is String -> entry
            is Map<*, *> -> entry["value"] as? String
                ?: throw GradleException("'$key' entry $index in $filePath must have a string 'value'")

            else -> throw GradleException("'$key' entry $index in $filePath must be a string or object")
        }
    }

    val duplicates = values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.sorted()
    if (duplicates.isNotEmpty()) {
        throw GradleException(
            buildString {
                appendLine("Duplicate '$key' entrypoints in $filePath:")
                duplicates.forEach { appendLine("  - $it") }
            }.trimEnd(),
        )
    }

    return values.toSortedSet()
}

private data class Entrypoints(
    val server: Set<String>,
    val client: Set<String>,
)

private data class VerificationProblems(
    val missingServer: Set<String>,
    val staleServer: Set<String>,
    val missingClient: Set<String>,
    val staleClient: Set<String>,
    val serverDeclaredAsClient: Set<String>,
    val clientDeclaredAsServer: Set<String>,
    val declaredUnderBothKeys: Set<String>,
) {
    fun isNotEmpty(): Boolean =
        missingServer.isNotEmpty() || staleServer.isNotEmpty() || missingClient.isNotEmpty() || staleClient.isNotEmpty() || serverDeclaredAsClient.isNotEmpty() || clientDeclaredAsServer.isNotEmpty() || declaredUnderBothKeys.isNotEmpty()

    fun format(): String = buildString {
        appendLine("GameTest entrypoints in fabric.mod.json do not match the compiled test classes.")
        appendProblem("Missing '$SERVER_ENTRYPOINT_KEY' entrypoints", missingServer)
        appendProblem("Stale '$SERVER_ENTRYPOINT_KEY' entrypoints", staleServer)
        appendProblem("Missing '$CLIENT_ENTRYPOINT_KEY' entrypoints", missingClient)
        appendProblem("Stale '$CLIENT_ENTRYPOINT_KEY' entrypoints", staleClient)
        appendProblem("Server GameTests declared as client GameTests", serverDeclaredAsClient)
        appendProblem("Client GameTests declared as server GameTests", clientDeclaredAsServer)
        appendProblem("Entrypoints declared under both GameTest keys", declaredUnderBothKeys)
    }.trimEnd()
}

private fun StringBuilder.appendProblem(heading: String, entries: Set<String>) {
    if (entries.isEmpty()) return
    appendLine()
    appendLine("$heading:")
    entries.sorted().forEach { appendLine("  - $it") }
}

private const val SERVER_ENTRYPOINT_KEY = "fabric-gametest"
private const val CLIENT_ENTRYPOINT_KEY = "fabric-client-gametest"
private const val SERVER_GAME_TEST_ANNOTATION = "net.fabricmc.fabric.api.gametest.v1.GameTest"
private const val CLIENT_GAME_TEST_INTERFACE = "net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest"
