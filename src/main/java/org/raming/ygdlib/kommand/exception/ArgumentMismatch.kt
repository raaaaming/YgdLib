package org.raming.ygdlib.kommand.exception

class ArgumentMismatch(val path: String) : Exception(path)