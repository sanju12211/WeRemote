package com.weremote.app.data

import com.weremote.app.ir.IrProtocols
import com.weremote.app.ir.IrSignal

/** Supported IR encodings. */
enum class Proto { NEC, SAMSUNG, SONY, RC5 }

/**
 * A single button's IR code.
 *  - NEC / SAMSUNG / RC5: [a] holds the full frame value; [b]/[bits] unused.
 *  - SONY: [a] = 7-bit command, [b] = device address, [bits] = 12/15/20.
 */
data class IrCode(
    val proto: Proto,
    val a: Long,
    val b: Int = 0,
    val bits: Int = 12
) {
    fun toSignal(): IrSignal = when (proto) {
        Proto.NEC -> IrProtocols.nec(a)
        Proto.SAMSUNG -> IrProtocols.samsung(a)
        Proto.SONY -> IrProtocols.sony(a.toInt(), b, bits)
        Proto.RC5 -> IrProtocols.rc5(a.toInt())
    }
}

/** Standard function keys used across remote layouts. */
object Fn {
    const val POWER = "POWER"
    const val VOL_UP = "VOL_UP"
    const val VOL_DOWN = "VOL_DOWN"
    const val CH_UP = "CH_UP"
    const val CH_DOWN = "CH_DOWN"
    const val MUTE = "MUTE"
    const val MENU = "MENU"
    const val HOME = "HOME"
    const val SOURCE = "SOURCE"
    const val BACK = "BACK"
    const val EXIT = "EXIT"
    const val UP = "UP"
    const val DOWN = "DOWN"
    const val LEFT = "LEFT"
    const val RIGHT = "RIGHT"
    const val OK = "OK"
    const val INFO = "INFO"
    const val D0 = "0"
    const val D1 = "1"
    const val D2 = "2"
    const val D3 = "3"
    const val D4 = "4"
    const val D5 = "5"
    const val D6 = "6"
    const val D7 = "7"
    const val D8 = "8"
    const val D9 = "9"
}

/** IR protocol families for stateful air-conditioner remotes. */
enum class AcProto { GREE, COOLIX, MIDEA, TCL, HAIER, ELECTRA, KELVINATOR, LG, HISENSE }

/** One brand/model codeset for a device type. */
data class Brand(
    val name: String,
    val codes: Map<String, IrCode>,
    /** When non-empty this brand is an air conditioner; each entry is a codeset to test. */
    val acCandidates: List<AcProto> = emptyList(),
    /** When true, opens a codeset-matching wizard that cycles all codesets of this type. */
    val isUniversal: Boolean = false
) {
    fun code(fn: String): IrCode? = codes[fn]
    val isAc: Boolean get() = acCandidates.isNotEmpty()
}

/** A device category (TV, Air Conditioner, ...) with its brands. */
data class DeviceType(
    val id: String,
    val displayName: String,
    val iconRes: Int,
    val brands: List<Brand>
)

/** A remote the user has saved to their home. */
data class SavedRemote(
    val id: String,
    val name: String,
    val typeId: String,
    val brandName: String,
    /** For AC remotes: the chosen protocol name (e.g. "GREE"). Empty for others. */
    val codeset: String = ""
)
