package com.somna.sleeptracker.domain.resilience

import android.os.SystemClock

class TimeAnchorValidator {

    data class TimestampAnchor(
        val wallClockTime: Long,
        val elapsedRealtime: Long
    )

    private var lastAnchor: TimestampAnchor? = null

    /**
     * Validates if a new timestamp is consistent with monotonic elapsed realtime.
     * Detects if device wall clock was artificially adjusted backwards or if a reboot occurred.
     */
    fun validateTimestamp(currentWallTime: Long = System.currentTimeMillis(), currentElapsed: Long = SystemClock.elapsedRealtime()): Long {
        val anchor = lastAnchor
        if (anchor == null) {
            lastAnchor = TimestampAnchor(currentWallTime, currentElapsed)
            return currentWallTime
        }

        val elapsedDiff = currentElapsed - anchor.elapsedRealtime
        if (elapsedDiff < 0) {
            // Device rebooted, reset anchor
            lastAnchor = TimestampAnchor(currentWallTime, currentElapsed)
            return currentWallTime
        }

        val wallDiff = currentWallTime - anchor.wallClockTime
        // If wall clock jumped backwards or jumped dramatically forward compared to elapsed time (> 60s skew)
        val skew = Math.abs(wallDiff - elapsedDiff)
        val sanitizedTime = if (skew > 60_000L && wallDiff < 0) {
            anchor.wallClockTime + elapsedDiff
        } else {
            currentWallTime
        }

        lastAnchor = TimestampAnchor(sanitizedTime, currentElapsed)
        return sanitizedTime
    }
}
