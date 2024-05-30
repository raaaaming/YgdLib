package org.raming.ygdlib.plugin

import com.google.common.reflect.ClassPath
import kotlinx.coroutines.*
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.raming.ygdlib.kommand.KommandExecuter
import org.raming.ygdlib.kommand.language.LanguageRegistry
import org.raming.ygdlib.YgdLib
import org.raming.ygdlib.extension.createFolder
import org.raming.ygdlib.extension.newInstance
import org.raming.ygdlib.registry.ClassRegistry
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.primaryConstructor

@Suppress("UnstableApiUsage")
abstract class BukkitPlugin : JavaPlugin(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("${this::class.simpleName}CoroutineScope") + Dispatchers.IO

    lateinit var classRegistry: ClassRegistry
        private set

    lateinit var globalLanguageRegistry: LanguageRegistry
        private set

    lateinit var languageRegistry: LanguageRegistry
        private set

    open fun onStart() {}

    open fun  onStop() {}

    @Deprecated("Replaced by onStart.", ReplaceWith("onStart()"))
    override fun onEnable() {
        dataFolder.createFolder()
        saveDefaultConfig()

        classRegistry = ClassRegistry()
        var languageFolder = File(dataFolder, "language").createFolder()
        if (this is YgdLib) {
            val globalLanguageFolder = File(dataFolder, "global-language").createFolder()
            globalLanguageRegistry = LanguageRegistry(globalLanguageFolder)
            globalLanguageRegistry.load(this, true)
        }
        globalLanguageRegistry = YgdLib.instance.globalLanguageRegistry
        languageRegistry = LanguageRegistry(languageFolder)
        languageRegistry.load(this)

        onStart()
        loadClass()
        registerCommand()
        registerListener()
    }

    private fun loadClass() {
        val classPath = ClassPath.from(super.getClassLoader()).getTopLevelClassesRecursive(this::class.java.packageName)
        classPath.forEach { classInfo ->
            val clazz = classInfo.load()
            val kClazz = clazz.kotlin
            if (kClazz.simpleName!!.endsWith("Kt") || clazz.isAnnotation) return@forEach
            classRegistry.add(kClazz)
        }
    }

    private fun registerCommand() {
        classRegistry.getInheritedClasses(KommandExecuter::class).forEach { clazz ->
            val kommandExecuter = clazz.primaryConstructor.newInstance<KommandExecuter>(this)
            kommandExecuter.register()
        }
    }

    private fun registerListener() {
        classRegistry.getInheritedClasses(Listener::class).forEach { clazz ->
            val listener = clazz.primaryConstructor.newInstance<Listener>(this)
            server.pluginManager.registerEvents(listener, this)
        }
    }

    @Deprecated("Replaced by onStop.", ReplaceWith("onStop()"))
    override fun onDisable() {
        onStop()
    }

}