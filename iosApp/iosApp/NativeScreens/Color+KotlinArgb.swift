import SwiftUI

extension Color {
    /// Builds a color from a Compose `Color.toArgb()` value.
    init(kotlinArgb: Int32) {
        let value = UInt32(bitPattern: kotlinArgb)
        self.init(
            .sRGB,
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255,
            opacity: Double((value >> 24) & 0xFF) / 255
        )
    }
}
