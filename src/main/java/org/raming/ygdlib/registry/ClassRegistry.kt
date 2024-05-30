package org.raming.ygdlib.registry

import kotlin.reflect.KClass
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf

class ClassRegistry {

    private val classes = mutableSetOf<KClass<*>>()

    fun <T : Any> getInheritedClasses(clazz: KClass<T>): List<KClass<*>> {
        return classes.filter { it.isSubclassOf(clazz) && it != clazz }
    }

    fun add(clazz: KClass<*>) {
        classes.add(clazz)
    }

    fun getAll(): Set<KClass<*>> = classes

}