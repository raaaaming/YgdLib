package org.raming.ygdlib.kommand.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubKommand(
    val argument: String,
    val permission: String = "",
    val isOp: Boolean = false,
    val hide: Boolean = false,
    val priority: Int = 1
)
