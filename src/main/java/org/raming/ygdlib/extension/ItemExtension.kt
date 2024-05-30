package org.raming.ygdlib.extension

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun ItemStack.setName(name: String): ItemStack {
    val meta = this.itemMeta
    meta.setDisplayName(name)
    this.itemMeta = meta
    return this
}

fun ItemStack.setLores(lores: List<String>): ItemStack {
    val meta = this.itemMeta
    meta.lore = lores
    this.itemMeta = meta
    return this
}

fun ItemStack.setCustomModelData(cmd: Int): ItemStack {
    val meta = this.itemMeta
    meta.setCustomModelData(cmd)
    this.itemMeta = meta
    return this
}

fun ItemStack.obtainName(): String = this.itemMeta.displayName

fun ItemStack.obtainLore(): List<String> = this.itemMeta.lore!!

fun ItemStack.obtainLoreToString(): String {
    var loreToString = ""
    this.obtainLore().let {
        it.forEachIndexed { index, s ->
        loreToString += s
        if (index != it.lastIndex) loreToString += " // "
        }
    }
    return loreToString
}

fun ItemStack.obtainCustomModelData(): Int = this.itemMeta.customModelData

fun ItemStack.setShiny(): ItemStack {
    val meta = this.itemMeta
    meta.let {
        it.addEnchant(Enchantment.DURABILITY, 100, true)
        it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }
    this.itemMeta = meta
    return this
}