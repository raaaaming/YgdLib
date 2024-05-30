package org.raming.ygdlib.command.provider

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.raming.ygdlib.YgdLib
import org.raming.ygdlib.kommand.argument.ArgumentProvider
import org.raming.ygdlib.kommand.exception.ArgumentMismatch

class IntegerArgumentProvider(
    private val plugin: YgdLib
) : ArgumentProvider<Int> {

    override fun cast(sender: CommandSender, argument: String?): Int {
        if (argument == null) {
            throw ArgumentMismatch("input-code")
        }
        return argument.toIntOrNull() ?: throw ArgumentMismatch("only-integer")
    }

    override fun getTabComplete(sender: CommandSender, location: Location?): List<String>? {
        return emptyList()
    }
}