package org.raming.ygdlib.command.provider

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.raming.ygdlib.kommand.argument.ArgumentProvider

class StringArgumentProvider : ArgumentProvider<String> {

    override fun cast(sender: CommandSender, argument: String?): String {
        return argument ?: throw NullPointerException("값 입력해주셈")
    }

    override fun getTabComplete(sender: CommandSender, location: Location?): List<String>? {
        return emptyList()
    }
}