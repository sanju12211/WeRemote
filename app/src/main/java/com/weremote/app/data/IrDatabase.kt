package com.weremote.app.data

import com.weremote.app.R

/**
 * The bundled universal IR code database.
 *
 * v1 ships verified, working codesets for the major TV brands (Samsung, LG,
 * Sony). The structure supports any number of device types and brands — add
 * new [Brand] entries with their [IrCode]s and they light up automatically in
 * the UI. Other device categories are listed so the browsing flow matches a
 * full universal remote; add codesets to enable them.
 */
object IrDatabase {

    // ---- TV brands -------------------------------------------------------

    private val samsungTv = Brand(
        "Samsung", mapOf(
            Fn.POWER to nec6(0xE0E040BF, sam = true),
            Fn.VOL_UP to sam(0xE0E0E01F), Fn.VOL_DOWN to sam(0xE0E0D02F),
            Fn.CH_UP to sam(0xE0E048B7), Fn.CH_DOWN to sam(0xE0E008F7),
            Fn.MUTE to sam(0xE0E0F00F), Fn.MENU to sam(0xE0E058A7),
            Fn.SOURCE to sam(0xE0E0807F), Fn.HOME to sam(0xE0E09E61),
            Fn.BACK to sam(0xE0E01AE5), Fn.EXIT to sam(0xE0E0B44B),
            Fn.UP to sam(0xE0E006F9), Fn.DOWN to sam(0xE0E08679),
            Fn.LEFT to sam(0xE0E0A659), Fn.RIGHT to sam(0xE0E046B9),
            Fn.OK to sam(0xE0E016E9), Fn.INFO to sam(0xE0E0F807),
            Fn.D1 to sam(0xE0E020DF), Fn.D2 to sam(0xE0E0A05F), Fn.D3 to sam(0xE0E0609F),
            Fn.D4 to sam(0xE0E010EF), Fn.D5 to sam(0xE0E0906F), Fn.D6 to sam(0xE0E050AF),
            Fn.D7 to sam(0xE0E030CF), Fn.D8 to sam(0xE0E0B04F), Fn.D9 to sam(0xE0E0708F),
            Fn.D0 to sam(0xE0E08877)
        )
    )

    private val lgTv = Brand(
        "LG", mapOf(
            Fn.POWER to nec(0x20DF10EF),
            Fn.VOL_UP to nec(0x20DF40BF), Fn.VOL_DOWN to nec(0x20DFC03F),
            Fn.CH_UP to nec(0x20DF00FF), Fn.CH_DOWN to nec(0x20DF807F),
            Fn.MUTE to nec(0x20DF906F), Fn.MENU to nec(0x20DFC23D),
            Fn.SOURCE to nec(0x20DFD02F), Fn.HOME to nec(0x20DF3EC1),
            Fn.BACK to nec(0x20DF14EB), Fn.EXIT to nec(0x20DFDA25),
            Fn.UP to nec(0x20DF02FD), Fn.DOWN to nec(0x20DF827D),
            Fn.LEFT to nec(0x20DFE01F), Fn.RIGHT to nec(0x20DF609F),
            Fn.OK to nec(0x20DF22DD), Fn.INFO to nec(0x20DF55AA),
            Fn.D1 to nec(0x20DF8877), Fn.D2 to nec(0x20DF48B7), Fn.D3 to nec(0x20DFC837),
            Fn.D4 to nec(0x20DF28D7), Fn.D5 to nec(0x20DFA857), Fn.D6 to nec(0x20DF6897),
            Fn.D7 to nec(0x20DFE817), Fn.D8 to nec(0x20DF18E7), Fn.D9 to nec(0x20DF9867),
            Fn.D0 to nec(0x20DF08F7)
        )
    )

    private const val SONY_TV = 1
    private val sonyTv = Brand(
        "Sony", mapOf(
            Fn.POWER to sony(21), Fn.VOL_UP to sony(18), Fn.VOL_DOWN to sony(19),
            Fn.CH_UP to sony(16), Fn.CH_DOWN to sony(17), Fn.MUTE to sony(20),
            Fn.MENU to sony(96), Fn.SOURCE to sony(37), Fn.HOME to sony(96),
            Fn.UP to sony(116), Fn.DOWN to sony(117), Fn.LEFT to sony(52),
            Fn.RIGHT to sony(51), Fn.OK to sony(101), Fn.INFO to sony(58),
            Fn.D1 to sony(0), Fn.D2 to sony(1), Fn.D3 to sony(2), Fn.D4 to sony(3),
            Fn.D5 to sony(4), Fn.D6 to sony(5), Fn.D7 to sony(6), Fn.D8 to sony(7),
            Fn.D9 to sony(8), Fn.D0 to sony(9)
        )
    )

    // Singer TVs (Bangladesh) are OEM sets; NEC is their most common protocol,
    // so we offer the NEC codeset as the first candidate to test.
    private val singerTv = lgTv.copy(name = "Singer")

    // ---- Air conditioner brands (stateful, Gree protocol) ----------------

    private val acCodesets = AcProto.values().toList()
    private val singerAc = Brand("Singer", emptyMap(), acCodesets)
    private val greeAc = Brand("Gree", emptyMap(), acCodesets)

    // ---- Device types ----------------------------------------------------

    val types: List<DeviceType> = listOf(
        DeviceType("tv", "TV", R.drawable.ic_tv, listOf(lgTv, samsungTv, singerTv, sonyTv)),
        DeviceType("stb", "Set Top Box", R.drawable.ic_stb, emptyList()),
        DeviceType("ac", "Air Conditioner", R.drawable.ic_ac, listOf(singerAc, greeAc)),
        DeviceType("dvd", "DVD", R.drawable.ic_dvd, emptyList()),
        DeviceType("projector", "Projector", R.drawable.ic_projector, emptyList()),
        DeviceType("amplifier", "Stereo Amplifier", R.drawable.ic_amplifier, emptyList()),
        DeviceType("fan", "Electric Fan", R.drawable.ic_fan, emptyList()),
        DeviceType("box", "TV Box", R.drawable.ic_stb, emptyList())
    )

    fun type(id: String): DeviceType? = types.firstOrNull { it.id == id }

    fun brand(typeId: String, brandName: String): Brand? =
        type(typeId)?.brands?.firstOrNull { it.name == brandName }

    // ---- helpers ---------------------------------------------------------

    private fun nec(value: Long) = IrCode(Proto.NEC, value)
    private fun sam(value: Long) = IrCode(Proto.SAMSUNG, value)
    private fun sony(cmd: Int) = IrCode(Proto.SONY, cmd.toLong(), SONY_TV, 12)

    // kept for readability of the Samsung POWER line
    private fun nec6(value: Long, sam: Boolean) = if (sam) sam(value) else nec(value)
}
