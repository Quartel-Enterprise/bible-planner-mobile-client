package com.quare.bibleplanner.tools.agentcli.command

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

sealed interface Command {
    val changesScreen: Boolean

    data object Help : Command {
        override val changesScreen: Boolean = false
    }

    data class Routes(
        val filter: String?,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data class Open(
        val route: String,
        val arguments: JsonObject,
        val isReplacingTop: Boolean,
    ) : Command {
        override val changesScreen: Boolean = true
    }

    data object Back : Command {
        override val changesScreen: Boolean = true
    }

    data object Reset : Command {
        override val changesScreen: Boolean = true
    }

    data object Stack : Command {
        override val changesScreen: Boolean = false
    }

    data class State(
        val path: String?,
        val maxItems: Int,
        val isApp: Boolean,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data class Events(
        val viewModel: String?,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data class Event(
        val target: String,
        val arguments: JsonObject,
    ) : Command {
        override val changesScreen: Boolean = true
    }

    data class Functions(
        val viewModel: String?,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data class Call(
        val target: String,
        val arguments: JsonObject,
        val maxItems: Int,
    ) : Command {
        override val changesScreen: Boolean = true
    }

    data class Settle(
        val quiet: Duration,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data class Wait(
        val path: String,
        val expected: JsonElement?,
        val timeout: Duration,
        val isApp: Boolean,
    ) : Command {
        override val changesScreen: Boolean = false
    }

    data object Log : Command {
        override val changesScreen: Boolean = false
    }

    data object Quit : Command {
        override val changesScreen: Boolean = false
    }
}
