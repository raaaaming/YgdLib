package org.raming.ygdlib.kommand.language

import org.bukkit.configuration.file.YamlConfiguration
import org.raming.ygdlib.kommand.page.PageHelper
import org.raming.ygdlib.extension.applyColor

class Language(
    private val config: YamlConfiguration
) {

    fun getMessage(path: String): String {
        return getString(path).applyColor()
    }

    fun getDescription(root: String, argument: String): String {
        return getString(getPath(root, argument, "description")).applyColor()
    }

    fun getArguments(root: String, argument: String): String {
        return getString(getPath(root, argument, "arguments")).applyColor()
    }

    fun getErrorMessage(path: String): String {
        return getString("error.$path").applyColor()
    }

    fun getArgumentErrorMessage(root: String, argument: String, path: String): String {
        return getString(getPath(root, argument, "error.$path")).applyColor()
    }

    fun getPageHelper(): PageHelper {
        return PageHelper(config).also { it.load() }
    }

    private fun getString(path: String): String {
        return config.getString(path) ?: throw NullPointerException("$path is not exists in language.")
    }

    private fun getPath(root: String, argument: String, path: String): String {
        return "command.$root.$argument.$path"
    }
}