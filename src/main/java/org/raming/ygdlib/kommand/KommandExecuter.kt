package org.raming.ygdlib.kommand

import com.sun.jdi.InvocationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.raming.ygdlib.kommand.annotation.BukkitAsync
import org.raming.ygdlib.kommand.annotation.Kommand
import org.raming.ygdlib.kommand.annotation.SubKommand
import org.raming.ygdlib.kommand.argument.ArgumentProvider
import org.raming.ygdlib.kommand.argument.ArgumentProviderRegistry
import org.raming.ygdlib.kommand.argument.ArgumentRegistry
import org.raming.ygdlib.kommand.exception.ArgumentMismatch
import org.raming.ygdlib.kommand.language.Language
import org.raming.ygdlib.kommand.language.LanguageRegistry
import org.raming.ygdlib.kommand.page.PageType
import org.raming.ygdlib.plugin.BukkitPlugin
import org.raming.ygdlib.extension.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

abstract class KommandExecuter(
    private val plugin: BukkitPlugin,
) : CommandExecutor, TabCompleter {

    private var exception: KClass<out Exception>

    private var pluginCommand: PluginCommand

    private val argumentProviderRegistry: ArgumentProviderRegistry

    private val argumentRegistry: ArgumentRegistry

    private val globalLanguageRegistry: LanguageRegistry

    private val languageRegistry: LanguageRegistry

    init {
        val kommand = this::class.findAnnotation<Kommand>()
            ?: throw NullPointerException("Kommand Annotation is not registered.")
        kommand.also {
            val command = it.command
            exception = it.exception
            pluginCommand = plugin.getCommand(command)
                ?: throw NullPointerException("$command is not registered in plugin.yml.")
        }
        argumentProviderRegistry = ArgumentProviderRegistry()
        plugin.classRegistry.getInheritedClasses(ArgumentProvider::class).forEach { clazz ->
            val argumentProvider = clazz.primaryConstructor.newInstance<ArgumentProvider<*>>(plugin)
            argumentProviderRegistry.register(argumentProvider)
        }
        argumentRegistry = ArgumentRegistry()
        this::class.memberFunctions.filterIsInstance<KFunction<Unit>>().forEach {function ->
            val subKommand = function.findAnnotation<SubKommand>() ?: return@forEach
            val commandArgument = CommandArgument(subKommand, function)
            argumentRegistry.set(subKommand.argument, commandArgument)
        }
        globalLanguageRegistry = plugin.globalLanguageRegistry
        languageRegistry = plugin.languageRegistry
    }

    fun register() {
        pluginCommand.setExecutor(this)
        pluginCommand.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender is Player) {
                runDefaultCommand(sender, label)
            } else {
                runDefaultCommand(sender, label)
            }
            return true
        }
        val globalLanguage = globalLanguageRegistry.get(sender)
        val language = languageRegistry.get(sender)
        val input = args[0]
        if (input == "도움말") {
            if (args.size == 1) {
                val message = globalLanguage.getErrorMessage("input-page")
                sender.sendMessage(message)
                return true
            }
            val page = args[1].toIntOrNull() ?: run {
                val message = globalLanguage.getErrorMessage("only-integer")
                sender.sendMessage(message)
                return true
            }
            if (!printHelp(sender, label, page)) {
                val message = globalLanguage.getErrorMessage("not-exist-page")
                sender.sendMessage(message)
                return true
            }
            return true
        }
        val argument = argumentRegistry.get(input) ?: run {
            val message = globalLanguage.getErrorMessage("not-exist-command")
            sender.sendMessage(message)
            return true
        }
        argument.runArgument(sender, args.copyOfRange(1, args.size), globalLanguage, language)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return emptyList()
    }

    open fun runDefaultCommand(sender: CommandSender, label: String) {
        printHelp(sender, label, 1)
    }

    open fun runDefaultCommand(sender: Player, label: String) {
        printHelp(sender, label, 1)
    }

    private fun printHelp(sender: CommandSender, label: String, page: Int): Boolean {
        val arguments = argumentRegistry.getValues().filter {
            it.hasPermission(sender) && !it.isHide()
        }.sortedBy {
            it.subKommand.priority
        }
        val chunkedArguments = arguments.chunked(5)

        val maxPage = chunkedArguments.size
        val realPage = page - 1
        val nowPageArguments = chunkedArguments.getOrNull(realPage) ?: return false

        val globalLanguage = globalLanguageRegistry.get(sender)
        val language = languageRegistry.get(sender)
        sender.sendMessage("")
        nowPageArguments.forEach {
            it.printDescription(sender, label, language)
        }
        val pageHelper = globalLanguage.getPageHelper()

        var beforePageElement = pageHelper.getPageElement(PageType.BEFORE)
        var currentPageElement = pageHelper.getPageElement(PageType.CURRENT)
        var nextPageElement = pageHelper.getPageElement(PageType.NEXT)

        var pageComponent = if (maxPage > 1 &&
            beforePageElement != null && currentPageElement != null && nextPageElement != null
            ) {
            val beforePageComponent = createPageComponent(
                beforePageElement.display,
                beforePageElement.showText,
                "/$label help ${page - 1}"
            )
            val currentPageComponent = createPageComponent(
                currentPageElement.display
                    .replace("%current%", "$page")
                    .replace("%max%", "$maxPage"),
                currentPageElement.showText
                    .replace("%current%", "$page")
            )
            val nextPageComponent = createPageComponent(
                nextPageElement.display,
                nextPageElement.showText,
                "/$label help ${page + 1}"
            )
            TextComponent().apply {
                addExtra(beforePageComponent)
                addExtra(currentPageComponent)
                addExtra(nextPageComponent)
            }
        } else {
            null
        }
        sender.sendMessage("")
        if (pageComponent != null) {
            sender.spigot().sendMessage(pageComponent)
        }
        return true
    }

    private fun createPageComponent(display: String, showText: String, command: String? = null): TextComponent {
        return TextComponent(display).apply {
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, showText.textComponent.toArray())
            if (command != null) {
                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
            }
        }
    }

    internal inner class CommandArgument(
        val subKommand: SubKommand,
        private val function: KFunction<Unit>
    ) {

        private var isBukkitAsync = function.hasAnnotation<BukkitAsync>()

        private val argumentParameters = ArrayList<Pair<ArgumentProvider<*>, Boolean>>()

        init {
            function.valueParameters.forEachIndexed { index, parameter ->
                if (index < 1) return@forEachIndexed

                val type = parameter.type
                val clazz = type.jvmErasure
                val argument = argumentProviderRegistry.get(clazz) ?: return@forEachIndexed

                argumentParameters.add(argument to !type.isMarkedNullable)
            }
        }

        fun isHide() : Boolean = subKommand.hide

        fun hasPermission(sender: CommandSender): Boolean {
            if (!subKommand.isOp) return true
            if (sender.isOp) return true
            if (subKommand.permission != "" && sender.hasPermission(subKommand.permission)) return true
            return false
        }

        fun printDescription(sender: CommandSender, label: String, language: Language) {
            val rootCommandLabel = pluginCommand.name
            val argumentLabel = subKommand.argument
            val arguments = language.getArguments(rootCommandLabel, argumentLabel)
            val argumentDescription = language.getDescription(rootCommandLabel, argumentLabel)
            val description = "&6/$label $argumentLabel &7$arguments &8- &f$argumentDescription".applyColor()
            sender.sendMessage(description)
        }

        fun runArgument(sender: CommandSender, args: Array<out String>, globalLanguage: Language, language: Language) {
            val functionParameters = function.valueParameters
            val firstParameter = functionParameters.firstOrNull() ?: return
            if (sender is ConsoleCommandSender && firstParameter.type.classifier == Player::class) {
                val message = globalLanguage.getErrorMessage("command-only-player")
                sender.sendMessage(message)
                return
            }
            if (!hasPermission(sender)) {
                val message = globalLanguage.getMessage("has-not-permission")
                sender.sendMessage(message)
                return
            }
            val parameters = argumentParameters.mapIndexed { index, pair ->
                val input = if (args.size > index) args[index] else null
                if (pair.second) {
                    try {
                        pair.first.cast(sender, input)
                    } catch (e: ArgumentMismatch) {
                        val message = language.getArgumentErrorMessage(
                            pluginCommand.name,
                            subKommand.argument,
                            e.path,
                        ).applyColor()
                        sender.sendMessage(message)
                        return
                    }
                } else {
                    if (input == null) {
                        null
                    } else {
                        pair.first.cast(sender, input)
                    }
                }
            }.toMutableList()
            val lastParameter = functionParameters.lastOrNull()
            if (lastParameter != null && lastParameter.isVararg) {
                println("functionParameters: ${functionParameters.map { it.type.jvmErasure.simpleName }}")
                println("args: ${args.toList()}")
                val copyArgs = args.copyOfRange(functionParameters.size - 2, args.size)
                println("copyArgs: ${copyArgs.toList()}")
                parameters.add(copyArgs)
            }
            println("parameters: ${parameters.map { if (it is Array<*>) it.toList() else it.toString() }}")
            if (function.isSuspend) {
                println("function call suspend")
                plugin.launch(CoroutineExceptionHandler { _, e ->
                    handleException(sender, language, e)
                }) {
                    suspendCatching(sender, language) {
                        function.callSuspend(this@KommandExecuter, sender, *parameters.toTypedArray())
                    }
                }
                return
            }
            if (isBukkitAsync) {
                println("function call bukkit async")
                plugin.async {
                    catching(sender, language) {
                        function.call(this@KommandExecuter, sender, *parameters.toTypedArray())
                    }
                }
                return
            }
            println("function call")
            catching(sender, language) {
                function.call(this@KommandExecuter, sender, *parameters.toTypedArray())
            }
        }
    }

    private suspend fun suspendCatching(sender: CommandSender, language: Language, actionFunction: suspend () -> Unit) {
        try {
            actionFunction()
        } catch (e: InvocationException) {
            handleException(sender, language, e.cause)
        }
    }

    private fun catching(sender: CommandSender, language: Language, actionFunction: () -> Unit) {
        try {
            actionFunction()
        } catch (e: InvocationException) {
            handleException(sender, language, e.cause)
        }
    }

    private fun handleException(sender: CommandSender, language: Language, cause: Throwable?) {
        if (cause != null && cause::class.isSubclassOf(exception)) {
            val message = cause.message
            if (message != null) {
                val errorMessage = language.getMessage(message)
                sender.sendMessage(errorMessage)
            }
        } else {
            cause?.printStackTrace()
        }
    }

}