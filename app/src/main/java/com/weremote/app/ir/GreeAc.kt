package com.weremote.app.ir

/** Mutable air-conditioner state shared by all AC protocols. */
data class AcState(
    var power: Boolean = true,
    var mode: Int = 1,      // 0 auto, 1 cool, 2 dry, 3 fan, 4 heat
    var temp: Int = 24,     // 16..30 C
    var fan: Int = 0,       // 0 auto, 1 low, 2 med, 3 high
    var swing: Boolean = false
) {
    fun clampTemp() { if (temp < 16) temp = 16; if (temp > 30) temp = 30 }
}

/**
 * Gree YAW1F/YB1F protocol encoder (used by Gree and many OEM brands such as
 * Singer, Chigo, etc. in South Asia).
 *
 * Frame: 8 state bytes sent LSB-first as two 32-bit blocks. Between the blocks
 * a fixed 3-bit marker (0b010) and a long message gap are inserted. 38 kHz.
 */
object GreeAc {

    private const val HDR_MARK = 9000
    private const val HDR_SPACE = 4500
    private const val BIT_MARK = 620
    private const val ONE_SPACE = 1600
    private const val ZERO_SPACE = 540
    private const val MSG_SPACE = 19000

    fun bytes(s: AcState): IntArray {
        s.clampTemp()
        val b = IntArray(8)
        // byte0: mode(0-2), power(3), fan(4-5), swing-auto(6)
        b[0] = (s.mode and 0x7) or
            (if (s.power) 0x8 else 0) or
            ((s.fan and 0x3) shl 4) or
            (if (s.swing) 0x40 else 0)
        // byte1: temp - 16 (0-3)
        b[1] = (s.temp - 16) and 0xF
        // byte3: fixed high nibble 0b0101
        b[3] = 0x50
        // byte4: vertical swing position (1 = full swing when enabled)
        b[4] = if (s.swing) 0x1 else 0x0
        // byte7 high nibble: checksum
        var sum = 10
        sum += (b[0] and 0xF) + (b[1] and 0xF) + (b[2] and 0xF) + (b[3] and 0xF)
        sum += ((b[4] shr 4) and 0xF) + ((b[5] shr 4) and 0xF) + ((b[6] shr 4) and 0xF)
        b[7] = (b[7] and 0x0F) or ((sum and 0xF) shl 4)
        return b
    }

    fun signal(s: AcState): IrSignal {
        val b = bytes(s)
        val p = ArrayList<Int>()
        p.add(HDR_MARK); p.add(HDR_SPACE)

        // Block 1: bytes 0..3, LSB first
        for (i in 0 until 4) addByte(p, b[i])
        // 3-bit marker 0b010 (sent 0,1,0)
        addBit(p, 0); addBit(p, 1); addBit(p, 0)
        // message gap
        p.add(BIT_MARK); p.add(MSG_SPACE)

        // Block 2: bytes 4..7, LSB first
        for (i in 4 until 8) addByte(p, b[i])
        p.add(BIT_MARK) // footer

        return IrSignal(38000, p.toIntArray())
    }

    private fun addByte(p: ArrayList<Int>, value: Int) {
        for (bit in 0 until 8) addBit(p, (value shr bit) and 1)
    }

    private fun addBit(p: ArrayList<Int>, bit: Int) {
        p.add(BIT_MARK)
        p.add(if (bit == 1) ONE_SPACE else ZERO_SPACE)
    }
}
