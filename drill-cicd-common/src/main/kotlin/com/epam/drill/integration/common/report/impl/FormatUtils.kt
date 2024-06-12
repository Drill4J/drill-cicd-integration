package com.epam.drill.integration.common.report.impl

fun Double.percent() = "%.0f".format(this * 100)

fun String.wrapToLink(url: String?) = if (url != null) "[$this]($url)" else this

fun Int.pluralEnding(ending: String = "s") = if (this > 1) ending else ""