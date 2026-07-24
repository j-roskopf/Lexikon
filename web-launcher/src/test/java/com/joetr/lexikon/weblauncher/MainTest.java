package com.joetr.lexikon.weblauncher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void mimeTypes() {
        assertEquals("text/html; charset=utf-8", Main.mimeType("/index.html"));
        assertEquals("application/wasm", Main.mimeType("/lexikon.wasm"));
    }
}
