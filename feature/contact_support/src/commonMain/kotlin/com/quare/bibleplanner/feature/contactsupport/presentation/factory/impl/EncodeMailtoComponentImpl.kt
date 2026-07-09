package com.quare.bibleplanner.feature.contactsupport.presentation.factory.impl

import com.quare.bibleplanner.feature.contactsupport.presentation.factory.EncodeMailtoComponent

internal class EncodeMailtoComponentImpl : EncodeMailtoComponent {
    override fun invoke(value: String): String = buildString {
        for (byte in value.encodeToByteArray()) {
            val unsigned = byte.toInt() and 0xFF
            val char = unsigned.toChar()
            if (unsigned < ASCII_LIMIT && (char.isLetterOrDigit() || char in UNRESERVED_CHARS)) {
                append(char)
            } else {
                append('%')
                append(unsigned.toString(radix = HEX_RADIX).uppercase().padStart(HEX_LENGTH, '0'))
            }
        }
    }

    private companion object {
        const val ASCII_LIMIT = 128
        const val UNRESERVED_CHARS = "-_.~"
        const val HEX_RADIX = 16
        const val HEX_LENGTH = 2
    }
}
