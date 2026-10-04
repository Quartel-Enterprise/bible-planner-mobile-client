package com.quare.bibleplanner.tools.agentcli.catalog

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass

/*
 * Why: the polymorphic NavKey registrations that restore the back stack on iOS and the web already
 * list every route with its serializer, so the catalog reads them instead of keeping its own list.
 */
class RouteCatalog(
    private val json: Json,
    serializersModule: SerializersModule,
) {
    val routes: List<RouteDescriptor> = collectRoutes(serializersModule).sortedBy(RouteDescriptor::name)

    fun find(name: String): RouteDescriptor {
        routes.firstOrNull { route -> route.name.equals(name, ignoreCase = true) }?.let { return it }
        val bySimpleName = routes.filter { route -> route.kClass.simpleName.equals(name, ignoreCase = true) }
        return when (bySimpleName.size) {
            1 -> bySimpleName.single()
            0 -> throw IllegalArgumentException("unknown route $name; run `routes` to list them")
            else -> throw IllegalArgumentException("$name is ambiguous: ${bySimpleName.map(RouteDescriptor::name)}")
        }
    }

    fun nameOf(route: NavKey): String = routes.firstOrNull { it.kClass == route::class }?.name
        ?: route::class.simpleName.orEmpty()

    fun create(
        route: RouteDescriptor,
        arguments: JsonObject,
    ): NavKey = json.decodeFromJsonElement(route.serializer, arguments)

    @OptIn(ExperimentalSerializationApi::class)
    fun getSignature(route: RouteDescriptor): String {
        val descriptor = route.serializer.descriptor
        if (descriptor.elementsCount == 0) return ""
        return (0 until descriptor.elementsCount).joinToString(
            prefix = "(",
            postfix = ")",
        ) { index ->
            val element = descriptor.getElementDescriptor(index)
            val optional = if (descriptor.isElementOptional(index)) " = …" else ""
            "${descriptor.getElementName(index)}: ${element.toDisplayName()}$optional"
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun SerialDescriptor.toDisplayName(): String {
        val base = serialName.removeSuffix("?").substringAfterLast('.')
        val arguments = if (elementsCount > 0 && serialName.startsWith("kotlin.collections")) {
            (0 until elementsCount).joinToString(
                prefix = "<",
                postfix = ">",
            ) { index -> getElementDescriptor(index).toDisplayName() }
        } else {
            ""
        }
        return base + arguments + if (isNullable) "?" else ""
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun collectRoutes(serializersModule: SerializersModule): List<RouteDescriptor> {
        val routes = mutableListOf<RouteDescriptor>()
        serializersModule.dumpTo(
            object : SerializersModuleCollector {
                override fun <T : Any> contextual(
                    kClass: KClass<T>,
                    provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>,
                ) {}

                @Suppress("UNCHECKED_CAST")
                override fun <Base : Any, Sub : Base> polymorphic(
                    baseClass: KClass<Base>,
                    actualClass: KClass<Sub>,
                    actualSerializer: KSerializer<Sub>,
                ) {
                    if (baseClass == NavKey::class) {
                        routes += RouteDescriptor(
                            name = actualClass.qualifiedName.orEmpty().removePrefix(ROUTE_PACKAGE),
                            kClass = actualClass as KClass<out NavKey>,
                            serializer = actualSerializer as KSerializer<out NavKey>,
                        )
                    }
                }

                override fun <Base : Any> polymorphicDefaultSerializer(
                    baseClass: KClass<Base>,
                    defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?,
                ) {}

                override fun <Base : Any> polymorphicDefaultDeserializer(
                    baseClass: KClass<Base>,
                    defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?,
                ) {}
            },
        )
        return routes
    }

    private companion object {
        const val ROUTE_PACKAGE = "com.quare.bibleplanner.core.model.route."
    }
}
