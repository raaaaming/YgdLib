package org.raming.ygdlib.command.provider

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.raming.ygdlib.YgdLib
import org.raming.ygdlib.kommand.argument.ArgumentProvider
import org.raming.ygdlib.kommand.exception.ArgumentMismatch

class PlayerArgumentProvider(
    private val plugin: YgdLib
) : ArgumentProvider<Player> {

    override fun cast(sender: CommandSender, argument: String?): Player {
        if (argument == null) {
            throw ArgumentMismatch("input-player")
        }
        return plugin.server.getPlayer(argument) ?: throw ArgumentMismatch("player-offline")
    }

    override fun getTabComplete(sender: CommandSender, location: Location?): List<String> {
        return emptyList()
    }
}