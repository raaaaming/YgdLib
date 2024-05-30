package org.raming.ygdlib.kommand.argument

import org.raming.ygdlib.kommand.KommandExecuter

internal class ArgumentRegistry {

    private val argumentMap = mutableMapOf<String, KommandExecuter.CommandArgument>()

    fun get(argument: String): KommandExecuter.CommandArgument? = argumentMap[argument]

    fun set(argument: String, commandArgument: KommandExecuter.CommandArgument) {
        argumentMap[argument] = commandArgument
    }

    fun getValues() : List<KommandExecuter.CommandArgument> = argumentMap.values.toList()
}