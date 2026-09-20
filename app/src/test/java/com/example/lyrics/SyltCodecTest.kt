package com.example.lyrics

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets

class SyltCodecTest {

    @Test
    fun unicodeTextIsSerializedAsUtf16BeWithTwoByteTerminator() {
        val text = "Café — 中文 😃"
        val timestamp = 123_456L

        val bytes = SyltCodec.serialize(
            listOf(LyricLine(timestamp, text))
        )

        val expected = text.toByteArray(StandardCharsets.UTF_16BE) +
            byteArrayOf(0, 0) +
            byteArrayOf(
                0x00,
                0x01,
                0xE2.toByte(),
                0x40.toByte()
            )

        assertArrayEquals(expected, bytes)
    }

    @Test
    fun negativeTimestampsAreClampedToZero() {
        val bytes = SyltCodec.serialize(listOf(LyricLine(-10L, "é")))

        assertEquals(10, bytes.size)
        assertArrayEquals(
            byteArrayOf(0xFE.toByte(), 0xFF.toByte()) +
                "é".toByteArray(StandardCharsets.UTF_16BE) +
                byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            bytes
        )
    }
}
