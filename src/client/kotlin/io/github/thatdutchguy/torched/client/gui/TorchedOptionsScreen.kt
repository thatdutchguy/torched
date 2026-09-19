package io.github.thatdutchguy.torched.client.gui

import io.github.thatdutchguy.torched.TorchedConfig
import io.github.thatdutchguy.torched.VanillaTorchThrowingPolicy
import io.github.thatdutchguy.torched.client.VanillaTorchThrowingPermissions
import io.github.thatdutchguy.torched.client.VanillaTorchThrowingPolicyClient
import io.github.thatdutchguy.torched.client.allowVanillaTorchThrowing
import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.options.OptionsSubScreen
import net.minecraft.network.chat.Component

class TorchedOptionsScreen(lastScreen: Screen) : OptionsSubScreen(
    lastScreen,
    Minecraft.getInstance().options,
    Component.translatable("options.torched.title"),
) {
    private val throwOnUse: OptionInstance<Boolean> = OptionInstance.createBoolean(
        "options.torched.throw_on_use",
        OptionInstance.cachedConstantTooltip(Component.translatable("options.torched.throw_on_use.tooltip")),
        TorchedConfig.data.throwOnUse,
    ) { value -> TorchedConfig.update { it.copy(throwOnUse = value) } }

    private val throwVanillaTorches: OptionInstance<Boolean> = OptionInstance.createBoolean(
        "options.torched.throw_vanilla_torches",
        OptionInstance.cachedConstantTooltip(
            Component.translatable("options.torched.throw_vanilla_torches.tooltip")
        ),
        TorchedConfig.data.throwVanillaTorches,
    ) { value ->
        TorchedConfig.update { it.copy(throwVanillaTorches = value) }
        VanillaTorchThrowingPolicy.publish()
    }

    private val throwVanillaTorchesServerDisabled =
        StringWidget(Component.translatable("options.torched.throw_vanilla_torches.server_disabled"), font)

    override fun addOptions() {
        // `list` is only null before init(), and addOptions() runs from within it.
        list!!.addSmall(throwOnUse, throwVanillaTorches)
        throwVanillaTorchesServerDisabled.visible = false
        layout.addToContents(throwVanillaTorchesServerDisabled)
    }

    override fun tick() {
        throwVanillaTorchesServerDisabled.visible =
            vanillaTorchThrowingServerDisabledWarningVisible(VanillaTorchThrowingPolicyClient.permissions)
    }
}

fun vanillaTorchThrowingServerDisabledWarningVisible(
    permissions: VanillaTorchThrowingPermissions,
    integratedServer: Boolean = Minecraft.getInstance().hasSingleplayerServer()
) =
    !integratedServer &&
            permissions.clientAllowed &&
            permissions.serverAllowed == false
