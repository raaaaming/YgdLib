package org.raming.ygdlib.extension

import java.lang.IllegalArgumentException
import kotlin.reflect.KFunction

@Suppress("unchecked_cast")
fun <T> KFunction<Any>?.newInstance(vararg args: Any?): T {
    return try {
        this?.call(*args)
    } catch (_: IllegalArgumentException) {
        this?.call()
    } as T
}
