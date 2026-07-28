package com.joetr.lexikon.web

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement

/**
 * Compose listens for key events on its canvas, so the physical keyboard is ignored until
 * that canvas holds DOM focus. Focus it as soon as it appears and take focus back whenever
 * it drifts, so the game accepts typing without being clicked first.
 */
fun installKeyboardBridge(root: Element) {
    var canvas: HTMLCanvasElement? = null

    fun focusCanvas() {
        val target = canvas ?: return
        if (document.activeElement !== target) target.focus()
    }

    // Compose creates the canvas once its runtime is ready, which is several frames after
    // main() runs, so watch for it instead of looking once.
    fun awaitCanvas(framesLeft: Int) {
        canvas = findCanvas(root)
        if (canvas != null) {
            focusCanvas()
            return
        }
        if (framesLeft > 0) {
            window.requestAnimationFrame { awaitCanvas(framesLeft - 1) }
        }
    }

    awaitCanvas(CANVAS_WAIT_FRAMES)
    window.addEventListener("focus", { focusCanvas() })
    document.addEventListener("pointerdown", { focusCanvas() })
}

/** Roughly ten seconds at 60fps — long enough for a cold load on a slow connection. */
private const val CANVAS_WAIT_FRAMES = 600

/** The canvas sits inside a shadow root, so a plain `querySelector` walk never reaches it. */
private fun findCanvas(node: Element): HTMLCanvasElement? {
    if (node is HTMLCanvasElement) return node
    node.shadowRoot?.let { shadow ->
        for (index in 0 until shadow.children.length) {
            shadow.children.item(index)?.let { child -> findCanvas(child)?.let { return it } }
        }
    }
    for (index in 0 until node.children.length) {
        node.children.item(index)?.let { child -> findCanvas(child)?.let { return it } }
    }
    return null
}
