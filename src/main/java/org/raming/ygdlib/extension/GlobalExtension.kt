package org.raming.ygdlib.extension

inline fun <reified T> T.toArray(): Array<T> {
    return arrayOf(this)
}