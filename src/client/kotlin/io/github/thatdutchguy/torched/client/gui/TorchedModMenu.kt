package io.github.thatdutchguy.torched.client.gui

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

object TorchedModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> TorchedOptionsScreen(parent) }
}
