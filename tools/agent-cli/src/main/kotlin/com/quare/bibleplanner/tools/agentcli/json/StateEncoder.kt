package com.quare.bibleplanner.tools.agentcli.json

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/*
 * Why: screen state is plain Kotlin (data classes, sealed hierarchies, resources) that is not
 * @Serializable, so it is walked with reflection instead of asking every UiState to opt in.
 */
class StateEncoder(
    private val stringResourceResolver: StringResourceResolver,
) {
    fun encode(
        value: Any?,
        maxItems: Int,
    ): JsonElement = Encoding(maxItems).encode(
        value = value,
        depth = 0,
    )

    private inner class Encoding(
        private val maxItems: Int,
    ) {
        private val visiting: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())

        fun encode(
            value: Any?,
            depth: Int,
        ): JsonElement = when {
            value == null -> JsonNull

            value is JsonElement -> value

            value is String -> JsonPrimitive(value)

            value is Boolean -> JsonPrimitive(value)

            value is Number -> JsonPrimitive(value)

            value is Char -> JsonPrimitive(value.toString())

            value is Enum<*> -> JsonPrimitive(value.name)

            depth >= MAX_DEPTH -> JsonPrimitive(TRUNCATED)

            value in visiting -> JsonPrimitive(CYCLE)

            else -> {
                visiting += value
                try {
                    encodeComposite(
                        value = value,
                        depth = depth + 1,
                    )
                } finally {
                    visiting -= value
                }
            }
        }

        private fun encodeComposite(
            value: Any,
            depth: Int,
        ): JsonElement = when (value) {
            is StringResource -> encodeStringResource(value)

            is Resource -> JsonPrimitive("@${value.getKindName()}/${value.getKeyOrId()}")

            is ImageVector -> JsonPrimitive("@icon/${value.name}")

            is Function<*> -> JsonPrimitive(FUNCTION)

            is Throwable -> JsonObject(
                mapOf(
                    "@error" to JsonPrimitive(value::class.simpleName),
                    "message" to JsonPrimitive(value.message),
                ),
            )

            is Map<*, *> -> encodeMap(
                map = value,
                depth = depth,
            )

            is Iterable<*> -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is Array<*> -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is IntArray -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is LongArray -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is FloatArray -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is BooleanArray -> encodeItems(
                items = value.toList(),
                depth = depth,
            )

            is ByteArray -> JsonPrimitive("<${value.size} bytes>")

            is KClass<*> -> JsonPrimitive(value.simpleName)

            else -> runCatching {
                encodeObject(
                    value = value,
                    depth = depth,
                )
            }.getOrElse { JsonPrimitive(value.toString()) }
        }

        private fun encodeStringResource(resource: StringResource): JsonElement {
            val text = runCatching { stringResourceResolver.resolve(resource) }.getOrNull()
            return JsonObject(
                mapOf(
                    "@string" to JsonPrimitive(resource.key),
                    "text" to JsonPrimitive(text),
                ),
            )
        }

        private fun encodeMap(
            map: Map<*, *>,
            depth: Int,
        ): JsonElement {
            val shown = if (maxItems > 0) map.entries.take(maxItems) else map.entries
            val content = shown.associate { (key, item) ->
                key.toString() to encode(
                    value = item,
                    depth = depth,
                )
            }
            val hidden = map.size - shown.size
            return JsonObject(if (hidden > 0) content + (MORE to JsonPrimitive(hidden)) else content)
        }

        private fun encodeItems(
            items: List<*>,
            depth: Int,
        ): JsonElement {
            val shown = if (maxItems > 0) items.take(maxItems) else items
            val encoded = shown.map { item ->
                encode(
                    value = item,
                    depth = depth,
                )
            }
            val hidden = items.size - shown.size
            return JsonArray(if (hidden > 0) encoded + JsonPrimitive("$MORE $hidden") else encoded)
        }

        private fun encodeObject(
            value: Any,
            depth: Int,
        ): JsonElement {
            val kClass = value::class
            val isAppClass = kClass.qualifiedName.orEmpty().startsWith(APP_PACKAGE)
            return when {
                kClass.objectInstance != null -> JsonPrimitive(kClass.simpleName)

                kClass.isValue && isAppClass -> encode(
                    value = kClass.getReadableProperties().firstOrNull()?.read(value),
                    depth = depth,
                )

                kClass.isData || (isAppClass && !kClass.isValue) -> encodeProperties(
                    value = value,
                    kClass = kClass,
                    depth = depth,
                )

                else -> JsonPrimitive(value.toString())
            }
        }

        private fun encodeProperties(
            value: Any,
            kClass: KClass<*>,
            depth: Int,
        ): JsonElement {
            val type = if (kClass.allSuperclasses.any(KClass<*>::isSealed)) {
                mapOf(TYPE to JsonPrimitive(kClass.simpleName))
            } else {
                emptyMap()
            }
            val properties = kClass.getReadableProperties().associate { property ->
                property.name to runCatching {
                    encode(
                        value = property.read(value),
                        depth = depth,
                    )
                }.getOrElse { error -> JsonPrimitive("<error: $error>") }
            }
            return JsonObject(type + properties)
        }
    }

    private fun KClass<*>.getReadableProperties(): List<KProperty1<out Any, *>> {
        val properties = memberProperties
        val constructorNames = primaryConstructor?.parameters?.mapNotNull { parameter -> parameter.name }
        return if (isData && constructorNames != null) {
            constructorNames.mapNotNull { name -> properties.firstOrNull { property -> property.name == name } }
        } else {
            properties.filter { property -> property.visibility == KVisibility.PUBLIC }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<out Any, *>.read(receiver: Any): Any? {
        isAccessible = true
        return (this as KProperty1<Any, *>).get(receiver)
    }

    private fun Resource.getKindName(): String = this::class
        .simpleName
        .orEmpty()
        .removeSuffix("Resource")
        .lowercase()

    private fun Resource.getKeyOrId(): String {
        val properties = this::class.memberProperties
        val property = properties.firstOrNull { it.name == "key" } ?: properties.firstOrNull { it.name == "id" }
        return property?.read(this)?.toString()?.substringAfter(':') ?: toString()
    }

    private companion object {
        const val APP_PACKAGE = "com.quare.bibleplanner"
        const val MAX_DEPTH = 32
        const val TRUNCATED = "<too deep>"
        const val CYCLE = "<cycle>"
        const val FUNCTION = "<function>"
        const val TYPE = "@type"
        const val MORE = "…more"
    }
}
