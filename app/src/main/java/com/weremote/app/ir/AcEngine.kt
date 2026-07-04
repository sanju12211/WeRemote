package com.weremote.app.ir

import com.weremote.app.data.AcProto

/**
 * Dispatches an [AcState] to one of several public air-conditioner IR
 * protocols. Each encoder is implemented from the public protocol spec
 * (no third-party code databases). The matching wizard lets the user try
 * each codeset and keep whichever one their AC responds to.
 */
object AcEngine {

    fun candidates(): List<AcProto> = AcProto.values().toList()

    fun label(p: AcProto): String = when (p) {
        AcProto.GREE -> "Gree (common)"
        AcProto.COOLIX -> "Coolix"
        AcProto.MIDEA -> "Midea"
        AcProto.TCL -> "TCL"
        AcProto.HAIER -> "Haier"
        AcProto.ELECTRA -> "Electra / AUX"
        AcProto.KELVINATOR -> "Kelvinator"
        AcProto.LG -> "LG"
        AcProto.HISENSE -> "Hisense"
    }

    fun signal(p: AcProto, s: AcState): IrSignal = when (p) {
        AcProto.GREE -> GreeAc.signal(s)
        AcProto.COOLIX -> CoolixAc.signal(s)
        AcProto.MIDEA -> MideaAc.signal(s)
        AcProto.TCL -> PulseAc.tcl(s)
        AcProto.HAIER -> PulseAc.haier(s)
        AcProto.ELECTRA -> PulseAc.electra(s)
        AcProto.KELVINATOR -> PulseAc.kelvinator(s)
        AcProto.LG -> PulseAc.lg(s)
        AcProto.HISENSE -> PulseAc.hisense(s)
    }
}

/**
 * Additional stateful AC protocols built on a generic pulse-distance frame.
 * Each uses its protocol's published carrier + header + bit timings and a
 * best-effort state byte layout. Implemented from public specs (open-source
 * IRremoteESP8266) — candidates for the matching wizard to try.
 */
object PulseAc {

    /** Build a pulse-distance frame from [bytes]. */
    private fun frame(
        freq: Int, hMark: Int, hSpace: Int, bMark: Int, one: Int, zero: Int,
        bytes: IntArray, lsbFirst: Boolean, gap: Int
    ): IrSignal {
        val p = ArrayList<Int>()
        p.add(hMark); p.add(hSpace)
        for (b in bytes) {
            if (lsbFirst) for (i in 0 until 8) bit(p, (b shr i) and 1, bMark, one, zero)
            else for (i in 7 downTo 0) bit(p, (b shr i) and 1, bMark, one, zero)
        }
        p.add(bMark); p.add(gap)
        return IrSignal(freq, p.toIntArray())
    }

    private fun bit(p: ArrayList<Int>, b: Int, m: Int, one: Int, zero: Int) {
        p.add(m); p.add(if (b == 1) one else zero)
    }

    private fun sum(bytes: IntArray, upto: Int): Int {
        var s = 0; for (i in 0 until upto) s += bytes[i]; return s and 0xFF
    }

    // 0 auto,1 cool,2 dry,3 fan,4 heat
    private fun t(s: AcState) = s.temp.coerceIn(16, 30)

    fun tcl(s: AcState): IrSignal {
        val mode = intArrayOf(0x8, 0x3, 0x2, 0x7, 0x1)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x0, 0x2, 0x3, 0x5)[s.fan.coerceIn(0, 3)]
        val b = intArrayOf(0x23, 0xCB, 0x26, 0x01, 0x00,
            (if (s.power) 0x24 else 0x20) or mode, (t(s) - 16) or 0x00,
            fan, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        b[13] = sum(b, 13)
        return frame(38000, 3000, 1650, 500, 1050, 325, b, true, 5000)
    }

    fun haier(s: AcState): IrSignal {
        val mode = intArrayOf(0x0, 0x2, 0x4, 0x6, 0x8)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x0, 0x2, 0x4, 0x6)[s.fan.coerceIn(0, 3)]
        val b = intArrayOf(0xA6, (t(s) - 16) shl 4, 0x00, mode or (if (s.power) 0x01 else 0),
            fan, 0x00, 0x00, 0x00, 0x00)
        b[8] = sum(b, 8)
        return frame(38000, 3000, 4300, 520, 1650, 650, b, false, 5000)
    }

    fun electra(s: AcState): IrSignal {
        val mode = intArrayOf(0x1, 0x3, 0x2, 0x7, 0x4)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x1, 0x3, 0x2, 0x5)[s.fan.coerceIn(0, 3)]
        val b = intArrayOf((if (s.power) 0x80 else 0) or (mode shl 4) or fan,
            (t(s) - 8) shl 3, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        b[12] = sum(b, 12)
        return frame(38000, 9166, 4470, 646, 1647, 547, b, false, 5000)
    }

    fun kelvinator(s: AcState): IrSignal {
        val mode = intArrayOf(0x0, 0x1, 0x2, 0x3, 0x4)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x0, 0x1, 0x2, 0x3)[s.fan.coerceIn(0, 3)]
        val b = IntArray(16)
        b[0] = mode or (if (s.power) 0x08 else 0) or (fan shl 4)
        b[1] = (t(s) - 16)
        b[3] = 0x50
        b[8] = b[0]; b[9] = b[1]; b[11] = 0x50
        return frame(38000, 9010, 4505, 680, 1530, 510, b, true, 19950)
    }

    fun lg(s: AcState): IrSignal {
        // 28-bit NEC-style value.
        val mode = intArrayOf(0x0, 0x0, 0x1, 0x2, 0x4)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x0, 0x0, 0x2, 0x4)[s.fan.coerceIn(0, 3)]
        val value = if (!s.power) 0x88C0051L
        else (0x8800000L or (mode.toLong() shl 16) or
            ((t(s) - 15).toLong() shl 8) or (fan.toLong() shl 4))
        val p = ArrayList<Int>()
        p.add(8000); p.add(4000)
        for (i in 27 downTo 0) bit(p, ((value shr i) and 1L).toInt(), 600, 1600, 550)
        p.add(600)
        return IrSignal(38000, p.toIntArray())
    }

    fun hisense(s: AcState): IrSignal {
        val mode = intArrayOf(0x0, 0x1, 0x2, 0x3, 0x4)[s.mode.coerceIn(0, 4)]
        val fan = intArrayOf(0x0, 0x1, 0x2, 0x3)[s.fan.coerceIn(0, 3)]
        val b = intArrayOf(0x08, 0xC0, 0x00, mode or (if (s.power) 0x10 else 0),
            (t(s) - 16) or (fan shl 4), 0x00, 0x00, 0x00)
        b[7] = sum(b, 7)
        return frame(38000, 8100, 4000, 600, 1650, 550, b, false, 5000)
    }
}

/**
 * Coolix (widely used by generic / OEM ACs). 24-bit messages; each byte is
 * followed by its complement, MSB-first, 38 kHz. The whole frame is sent twice.
 */
object CoolixAc {
    private const val HDR_MARK = 4692
    private const val HDR_SPACE = 4416
    private const val BIT_MARK = 552
    private const val ONE_SPACE = 1656
    private const val ZERO_SPACE = 552
    private const val GAP = 5040

    // Temperature map for 17..30 C (Coolix uses a non-linear nibble order).
    private val TEMP = intArrayOf(0x0, 0x1, 0x3, 0x2, 0x6, 0x7, 0x5, 0x4,
        0xC, 0xD, 0x9, 0x8, 0xA, 0xB)

    private fun code(s: AcState): Int {
        if (!s.power) return 0xB27BE0
        val t = s.temp.coerceIn(17, 30)
        val temp = TEMP[t - 17]
        val mode = when (s.mode) {   // 0 auto,1 cool,2 dry,3 fan,4 heat
            1 -> 0x0; 4 -> 0x1; 0 -> 0x2; 2 -> 0x3; else -> 0x2
        }
        val fan = when (s.fan) {     // 0 auto,1 low,2 med,3 high
            1 -> 0x9; 2 -> 0x5; 3 -> 0x1; else -> 0xB
        }
        // 0xB0 fixed high byte, then fan/temp/mode packed.
        return (0xB0 shl 16) or (fan shl 12) or (temp shl 8) or (mode shl 4) or 0x0
    }

    fun signal(s: AcState): IrSignal {
        val data = code(s)
        val frame = ArrayList<Int>()
        frame.add(HDR_MARK); frame.add(HDR_SPACE)
        for (shift in intArrayOf(16, 8, 0)) {
            val b = (data shr shift) and 0xFF
            addByte(frame, b)          // byte
            addByte(frame, b.inv() and 0xFF) // complement
        }
        frame.add(BIT_MARK); frame.add(GAP)

        val out = ArrayList<Int>()
        out.addAll(frame)
        out.addAll(frame)              // send twice
        out.add(BIT_MARK)
        return IrSignal(38000, out.toIntArray())
    }

    private fun addByte(p: ArrayList<Int>, value: Int) {
        for (i in 7 downTo 0) {        // MSB first
            val bit = (value shr i) and 1
            p.add(BIT_MARK); p.add(if (bit == 1) ONE_SPACE else ZERO_SPACE)
        }
    }
}

/**
 * Midea / Comfee / Pioneer. 3 data bytes + 3 complement bytes (48 bits),
 * MSB-first, 38 kHz, frame sent twice.
 */
object MideaAc {
    private const val HDR_MARK = 4480
    private const val HDR_SPACE = 4480
    private const val BIT_MARK = 560
    private const val ONE_SPACE = 1600
    private const val ZERO_SPACE = 560
    private const val GAP = 5100

    // Midea temperature order for 17..30 C.
    private val TEMP = intArrayOf(0x0, 0x1, 0x3, 0x2, 0x6, 0x7, 0x5, 0x4,
        0xC, 0xD, 0x9, 0x8, 0xA, 0xB)

    private fun code(s: AcState): Int {
        if (!s.power) return 0xB2_7B_E0
        val t = s.temp.coerceIn(17, 30)
        val temp = TEMP[t - 17]
        val fan = when (s.fan) {      // 0 auto,1 low,2 med,3 high
            1 -> 0x9; 2 -> 0x5; 3 -> 0x1; else -> 0xB
        }
        val mode = when (s.mode) {    // 0 auto,1 cool,2 dry,3 fan,4 heat
            1 -> 0x0; 2 -> 0x4; 3 -> 0x8; 4 -> 0xC; else -> 0x0
        }
        // 0xB2 fixed prefix, fan nibble, temp+mode.
        return (0xB2 shl 16) or (fan shl 12) or (temp shl 4) or (mode shr 2)
    }

    fun signal(s: AcState): IrSignal {
        val data = code(s)
        val frame = ArrayList<Int>()
        frame.add(HDR_MARK); frame.add(HDR_SPACE)
        for (shift in intArrayOf(16, 8, 0)) {
            val b = (data shr shift) and 0xFF
            addByte(frame, b)
            addByte(frame, b.inv() and 0xFF)
        }
        frame.add(BIT_MARK); frame.add(GAP)

        val out = ArrayList<Int>()
        out.addAll(frame)
        out.addAll(frame)
        out.add(BIT_MARK)
        return IrSignal(38000, out.toIntArray())
    }

    private fun addByte(p: ArrayList<Int>, value: Int) {
        for (i in 7 downTo 0) {
            val bit = (value shr i) and 1
            p.add(BIT_MARK); p.add(if (bit == 1) ONE_SPACE else ZERO_SPACE)
        }
    }
}
