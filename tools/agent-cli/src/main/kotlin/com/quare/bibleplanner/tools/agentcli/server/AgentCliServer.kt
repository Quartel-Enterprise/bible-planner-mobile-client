package com.quare.bibleplanner.tools.agentcli.server

import com.quare.bibleplanner.tools.agentcli.command.AgentCli
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/*
 * Why: a JVM, Koin and Room take seconds to start, so agents keep one process warm and send it
 * commands over loopback HTTP; each command then costs milliseconds.
 */
class AgentCliServer(
    private val cli: AgentCli,
    private val output: Json,
    private val onQuit: () -> Unit,
) {
    private lateinit var server: HttpServer

    fun start(port: Int): Int {
        server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0)
        server.executor = Executors.newSingleThreadExecutor()
        server.createContext("/", ::handle)
        server.start()
        return server.address.port
    }

    private fun handle(exchange: HttpExchange) {
        val isQuit = exchange.use { handleCommands(exchange) }
        if (isQuit) {
            server.stop(0)
            onQuit()
        }
    }

    private fun handleCommands(exchange: HttpExchange): Boolean {
        if (exchange.requestMethod != "POST") {
            respond(
                exchange = exchange,
                body = JsonObject(mapOf("ok" to JsonPrimitive(true))),
            )
            return false
        }
        val lines = exchange.requestBody
            .readBytes()
            .decodeToString()
            .lines()
            .map(String::trim)
            .filter { line -> line.isNotEmpty() && !line.startsWith("#") }
        val responses = lines.map { line -> runBlocking { cli.run(line) } }
        respond(
            exchange = exchange,
            body = responses.singleOrNull()?.json ?: JsonArray(responses.map(AgentCli.Response::json)),
        )
        return responses.any(AgentCli.Response::isQuit)
    }

    private fun respond(
        exchange: HttpExchange,
        body: JsonElement,
    ) {
        val bytes = (output.encodeToString(JsonElement.serializer(), body) + "\n").encodeToByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }
}
