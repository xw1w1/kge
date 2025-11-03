package kge.api.std

@JvmInline value class KGEWindowFlags(val bits: Int) {
    infix fun or(other: KGEWindowFlags) = KGEWindowFlags(bits or other.bits)

    infix fun and(other: KGEWindowFlags) = KGEWindowFlags(bits and other.bits)

    infix fun has(flag: KGEWindowFlags): Boolean = (bits and flag.bits) == flag.bits

    companion object {
        val None = KGEWindowFlags(0)

        val NoResize = KGEWindowFlags(1 shl 0)

        val NoFullscreen = KGEWindowFlags(1 shl 1)

        val NoClose = KGEWindowFlags(1 shl 3)
    }
}