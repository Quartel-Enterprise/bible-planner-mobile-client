package com.quare.bibleplanner.core.utils

/**
 * Returns the value of this [Boolean] if it is non-null, or `false` if it is `null`.
 *
 * This is a convenience extension for safely handling nullable [Boolean] values
 * without having to write explicit null checks. It is particularly useful when
 * working with optional flags or database values that may be undefined.
 *
 * Example usage:
 * ```
 * val isEnabled: Boolean? = null
 * val safeValue = isEnabled.orFalse() // returns false
 * ```
 *
 * @receiver The nullable [Boolean] to evaluate.
 * @return The original value if non-null, otherwise `false`.
 */
fun Boolean?.orFalse(): Boolean = this ?: false

/**
 * Returns the value of this [Boolean] if it is non-null, or `true` if it is `null`.
 *
 * This is a convenience extension for safely handling nullable [Boolean] values
 * without having to write explicit null checks. It is particularly useful when
 * you want the default assumption to be `true` in the absence of an explicit value,
 * such as feature flags, configuration options, or optional database fields.
 *
 * Example usage:
 * ```
 * val isVisible: Boolean? = null
 * val safeValue = isVisible.orTrue() // returns true
 * ```
 *
 * @receiver The nullable [Boolean] to evaluate.
 * @return The original value if non-null, otherwise `true`.
 */
fun Boolean?.orTrue(): Boolean = this ?: true
