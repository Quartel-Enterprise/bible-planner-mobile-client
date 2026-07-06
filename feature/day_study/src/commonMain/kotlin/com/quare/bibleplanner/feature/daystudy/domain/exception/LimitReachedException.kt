package com.quare.bibleplanner.feature.daystudy.domain.exception

/**
 * Signals that the user has no free analyses left (server returned 402 Payment Required).
 * Carried in the failure channel of [getDayStudy][com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository.getDayStudy]
 * so the caller can lock the card instead of treating it as a generic error.
 */
class LimitReachedException : Exception()
