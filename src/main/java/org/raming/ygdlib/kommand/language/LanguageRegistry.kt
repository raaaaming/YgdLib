package org.raming.ygdlib.kommand.language

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.raming.ygdlib.plugin.BukkitPlugin
import org.raming.ygdlib.extension.yml
import java.io.File

class LanguageRegistry(
    private val languageFolder: File
) {

    private val languageFiles get() = languageFolder.listFiles()

    private  val languageMap = mutableMapOf<String, Language>()

    fun load(plugin: BukkitPlugin, isGlobal: Boolean = false) {
        if (languageFiles?.any { it.name == "en_us.yml" } == false) {
            plugin.saveResource("${if (isGlobal) "global-" else ""}language/en_us.yml", false)
        }
        languageFiles?.filter {
            it.name.endsWith(".yml")
        }?.forEach { file ->
            val name = file.name.removeSuffix(".yml")
            languageMap[name] = Language(file.yml)
        }
    }

    fun get(sender: CommandSender): Language {
        return (sender as? Player)?.let { languageMap[sender.locale] } ?: languageMap["en_us"]!!
    }
}