package com.quare.bibleplanner.core.daystudy.domain.exception

// Why: mapped from the server's 402 Payment Required (no free analyses left); kept distinct
// so the caller locks the card instead of showing a generic error.
class LimitReachedException : Exception()
