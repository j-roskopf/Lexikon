package com.joetr.lexikon.web

import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

private var keyHandler: ((Char) -> Unit)? = null
private var backspaceHandler: (() -> Unit)? = null
private var submitHandler: (() -> Unit)? = null

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun installKeyboardBridge() {
    document.addEventListener("keydown", { event ->
        val e = event.unsafeCast<KeyboardEvent>()
        when (e.key) {
            "Backspace" -> { backspaceHandler?.invoke(); e.preventDefault() }
            "Enter" -> { submitHandler?.invoke(); e.preventDefault() }
            else -> if (e.key.length == 1 && e.key[0].isLetter()) {
                keyHandler?.invoke(e.key[0])
                e.preventDefault()
            }
        }
    })
}

fun registerWebKeyboardHandlers(
    onLetter: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
) {
    keyHandler = onLetter
    backspaceHandler = onBackspace
    submitHandler = onSubmit
}
