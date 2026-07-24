package com.joetr.lexikon.domain

actual fun sha256(bytes: ByteArray): ByteArray = WasmSha256.hash(bytes)

private object WasmSha256 {
    private val K = intArrayOf(
        0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b,
        0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
        -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
        -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039,
        -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, -0x7e3ba36e, -0x6d8dd37b,
        -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d,
        -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, -0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf7,
        -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e,
    )

    fun hash(message: ByteArray): ByteArray {
        val padded = pad(message)
        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19
        var offset = 0
        while (offset < padded.size) {
            val w = IntArray(64)
            for (i in 0 until 16) {
                val j = offset + i * 4
                w[i] = ((padded[j].toInt() and 0xFF) shl 24) or
                    ((padded[j + 1].toInt() and 0xFF) shl 16) or
                    ((padded[j + 2].toInt() and 0xFF) shl 8) or
                    (padded[j + 3].toInt() and 0xFF)
            }
            for (i in 16 until 64) {
                val s0 = rrot(w[i - 15], 7) xor rrot(w[i - 15], 18) xor (w[i - 15] ushr 3)
                val s1 = rrot(w[i - 2], 17) xor rrot(w[i - 2], 19) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }
            var a = h0; var b = h1; var c = h2; var d = h3
            var e = h4; var f = h5; var g = h6; var h = h7
            for (i in 0 until 64) {
                val s1 = rrot(e, 6) xor rrot(e, 11) xor rrot(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val t1 = h + s1 + ch + K[i] + w[i]
                val s0 = rrot(a, 2) xor rrot(a, 13) xor rrot(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val t2 = s0 + maj
                h = g; g = f; f = e; e = d + t1
                d = c; c = b; b = a; a = t1 + t2
            }
            h0 += a; h1 += b; h2 += c; h3 += d
            h4 += e; h5 += f; h6 += g; h7 += h
            offset += 64
        }
        return intToBytes(h0) + intToBytes(h1) + intToBytes(h2) + intToBytes(h3) +
            intToBytes(h4) + intToBytes(h5) + intToBytes(h6) + intToBytes(h7)
    }

    private fun pad(message: ByteArray): ByteArray {
        val bitLen = message.size.toLong() * 8
        val padLen = (56 - (message.size + 1) % 64 + 64) % 64
        val total = message.size + 1 + padLen + 8
        val out = ByteArray(total)
        message.copyInto(out)
        out[message.size] = 0x80.toByte()
        for (i in 0 until 8) out[total - 8 + i] = ((bitLen ushr (56 - i * 8)) and 0xFF).toByte()
        return out
    }

    private fun rrot(v: Int, bits: Int) = (v ushr bits) or (v shl (32 - bits))
    private fun intToBytes(v: Int) = byteArrayOf(
        ((v ushr 24) and 0xFF).toByte(),
        ((v ushr 16) and 0xFF).toByte(),
        ((v ushr 8) and 0xFF).toByte(),
        (v and 0xFF).toByte(),
    )
}
