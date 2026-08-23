package io.github.thatdutchguy.torched

import com.google.gson.GsonBuilder
import kotlinx.io.IOException
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

data class TorchedConfigData(
    val throwOnUse: Boolean = true, // client only
    val throwVanillaTorches: Boolean = true, // enforced by server
)

object TorchedConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    private val path: Path
        get() = FabricLoader.getInstance().configDir.resolve("torched.json")

    var data: TorchedConfigData = TorchedConfigData()
        private set

    fun update(transform: (TorchedConfigData) -> TorchedConfigData) {
        val updated = transform(data)
        if (updated == data) return
        data = updated
        save()
    }

    private class RawConfig {
        var throwOnUse: Boolean? = null
        var throwVanillaTorches: Boolean? = null
    }

    fun parse(json: String?): TorchedConfigData {
        val raw = try {
            GSON.fromJson(json, RawConfig::class.java)
        } catch (e: Exception) {
            Torched.logger.warn("Could not parse config, falling back to defaults", e)
            null
        }

        val defaults = TorchedConfigData()
        return TorchedConfigData(
            throwOnUse = raw?.throwOnUse ?: defaults.throwOnUse,
            throwVanillaTorches = raw?.throwVanillaTorches ?: defaults.throwVanillaTorches,
        )
    }

    fun load() {
        val file = path

        if (!Files.exists(file)) {
            Torched.logger.info("No config at {}, writing defaults", file)
            data = TorchedConfigData()
            save()
            return
        } else {
            val json = try {
                Files.readString(file)
            } catch (e: IOException) {
                Torched.logger.warn("Could not read {}, falling back to defaults", file, e)
                null
            }
            data = parse(json)
        }

        Torched.logger.info(
            "Throw on right-click: {}, vanilla torches throwable: {}",
            data.throwOnUse,
            data.throwVanillaTorches,
        )
    }

    fun save() {
        try {
            Files.createDirectories(path.parent)
            Files.newBufferedWriter(path).use { writer -> GSON.toJson(data, writer) }
        } catch (e: java.io.IOException) {
            Torched.logger.warn("Could not write {}", path, e)
        }
    }
}
