package org.raming.ygdlib.extension

import net.md_5.bungee.api.chat.TextComponent

val String.textComponent: TextComponent get() = TextComponent(this)

val List<String>.textComponents: List<TextComponent> get() = map(String::textComponent)