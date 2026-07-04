package com.weremote.app.ir

/**
 * An IR signal ready for ConsumerIrManager.transmit():
 *  - [frequency] carrier frequency in Hz (e.g. 38000)
 *  - [pattern]   alternating on/off durations in MICROSECONDS, starting with an "on" (mark)
 */
data class IrSignal(val frequency: Int, val pattern: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrSignal) return false
        return frequency == other.frequency && pattern.contentEquals(other.pattern)
    }
    override fun hashCode(): Int = 31 * frequency + pattern.contentHashCode()
}

/**
 * Encoders for the common consumer-IR protocols.
 *
 * The published "32-bit hex" values used by most online IR databases and the
 * Arduino-IRremote library are transmitted MSB-first, which is exactly what
 * [nec] and [samsung] do here.
 */
object IrProtocols {

    /** NEC protocol. Used by LG, TCL, Hisense, Toshiba, and many generic devices. */
    fun nec(value: Long): IrSignal {
        val p = ArrayList<Int>(68)
        p.add(9000); p.add(4500)            // leader
        for (i in 31 downTo 0) {
            val bit = (value ushr i) and 1L
            p.add(560)
            p.add(if (bit == 1L) 1690 else 560)
        }
        p.add(560)                          // final burst
        return IrSignal(38000, p.toIntArray())
    }

    /** Samsung protocol: same bit timing as NEC but a 4500/4500 leader. */
    fun samsung(value: Long): IrSignal {
        val p = ArrayList<Int>(68)
        p.add(4500); p.add(4500)            // leader
        for (i in 31 downTo 0) {
            val bit = (value ushr i) and 1L
            p.add(560)
            p.add(if (bit == 1L) 1690 else 560)
        }
        p.add(560)
        return IrSignal(38000, p.toIntArray())
    }

    /**
     * Sony SIRC. [bits] is 12, 15 or 20. [command] is the 7-bit function,
     * [address] the device code. Sent LSB-first and repeated 3x (Sony requires
     * at least 3 frames), each frame padded to a 45 ms period.
     */
    fun sony(command: Int, address: Int, bits: Int = 12): IrSignal {
        val addrBits = bits - 7
        val frame = ArrayList<Int>()
        frame.add(2400); frame.add(600)                 // header
        for (i in 0 until 7) {                          // 7 command bits, LSB first
            val b = (command ushr i) and 1
            frame.add(if (b == 1) 1200 else 600); frame.add(600)
        }
        for (i in 0 until addrBits) {                   // address bits, LSB first
            val b = (address ushr i) and 1
            frame.add(if (b == 1) 1200 else 600); frame.add(600)
        }
        // Duration of one frame so far
        var dur = 0
        for (v in frame) dur += v
        val gap = (45000 - dur).coerceAtLeast(10000)    // pad to ~45 ms period

        val out = ArrayList<Int>()
        for (rep in 0 until 3) {
            out.addAll(frame)
            out[out.size - 1] = out[out.size - 1] + gap // extend trailing space
        }
        return IrSignal(40000, out.toIntArray())
    }

    /**
     * Philips RC5 (14-bit, bi-phase / Manchester, 36 kHz).
     * [value] is the full 14-bit frame (2 start bits + toggle + 5 addr + 6 cmd).
     */
    fun rc5(value: Int): IrSignal {
        val half = 889 // half-bit time in us
        val out = ArrayList<Int>()
        // Manchester: for each bit, RC5 encodes 1 as (space,mark) and 0 as (mark,space).
        // We build a mark/space list then merge consecutive same-levels.
        val levels = ArrayList<Boolean>() // true = mark
        for (i in 13 downTo 0) {
            val bit = (value ushr i) and 1
            if (bit == 1) { levels.add(false); levels.add(true) }
            else { levels.add(true); levels.add(false) }
        }
        // Merge into durations, ensuring the pattern starts with a mark.
        var idx = 0
        // Drop a leading space if present (transmit must start with mark)
        if (levels.isNotEmpty() && !levels[0]) levels[0] = true
        while (idx < levels.size) {
            var run = 1
            while (idx + run < levels.size && levels[idx + run] == levels[idx]) run++
            out.add(half * run)
            idx += run
        }
        return IrSignal(36000, out.toIntArray())
    }
}
