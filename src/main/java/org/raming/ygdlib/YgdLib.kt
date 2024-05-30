package org.raming.ygdlib

import org.raming.ygdlib.plugin.BukkitPlugin

class YgdLib : BukkitPlugin() { //public class YGDP extends BukkitPlugin() {

    companion object {
        internal lateinit var instance: YgdLib
            private set
    }

    override fun onLoad() {
        instance = this
    }

    override fun onStart() {
        
    }

    override fun onStop() {
        
    }

}