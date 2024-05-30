package org.raming.ygdlib.exception

open class BukkitException(path: String) : Exception("error.$path")

class TestException(message2: String) : BukkitException(message2)