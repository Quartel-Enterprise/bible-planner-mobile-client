package com.quare.bibleplanner.tools.agentcli.server

import com.quare.bibleplanner.tools.agentcli.command.AgentCli
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.BufferedReader
import java.io.PrintStream

class Repl(
    private val cli: AgentCli,
    private val output: Json,
    private val input: BufferedReader,
    private val responses: PrintStream,
) {
    fun run() {
        for (rawLine in input.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            val response = runBlocking { cli.run(line) }
            responses.println(output.encodeToString(JsonElement.serializer(), response.json))
            if (response.isQuit) return
        }
    }
}
