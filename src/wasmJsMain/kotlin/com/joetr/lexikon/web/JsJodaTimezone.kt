@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.joetr.lexikon.web

import kotlin.js.JsModule

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

@Suppress("unused")
private val jsJodaTz = JsJodaTimeZoneModule

fun ensureJsJodaTimezoneLoaded() {
    jsJodaTz
}
