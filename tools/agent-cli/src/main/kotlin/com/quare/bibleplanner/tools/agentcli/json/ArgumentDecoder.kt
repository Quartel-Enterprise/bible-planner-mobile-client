package com.quare.bibleplanner.tools.agentcli.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/*
 * Why: UiEvents are not @Serializable, so an event typed on the command line is built through its
 * constructor; kotlinx.serialization is still tried first for every argument, which covers
 * primitives, enums, collections and the @Serializable models.
 */
class ArgumentDecoder(
    private val json: Json,
) {
    fun build(
        kClass: KClass<*>,
        arguments: JsonObject,
    ): Any {
        kClass.objectInstance?.let { instance ->
            require(arguments.isEmpty()) { "${kClass.simpleName} takes no arguments" }
            return instance
        }
        val constructor = requireNotNull(kClass.primaryConstructor) {
            "${kClass.simpleName} has no primary constructor"
        }
        constructor.isAccessible = true
        return constructor.callBy(
            bind(
                function = constructor,
                parameters = constructor.parameters,
                arguments = arguments,
            ),
        )
    }

    fun bind(
        function: KFunction<*>,
        parameters: List<KParameter>,
        arguments: JsonObject,
    ): Map<KParameter, Any?> {
        val names = parameters.mapNotNull(KParameter::name)
        val unknown = arguments.keys - names.toSet()
        require(unknown.isEmpty()) {
            "${function.name} has no parameter ${unknown.joinToString()}; expected ${getSignature(parameters)}"
        }
        return parameters
            .mapNotNull { parameter ->
                val element = arguments[parameter.name]
                when {
                    element != null -> parameter to decode(
                        type = parameter.type,
                        element = element,
                    )

                    parameter.isOptional -> null

                    parameter.type.isMarkedNullable -> parameter to null

                    else -> throw IllegalArgumentException(
                        "missing ${parameter.name}: ${parameter.type.toDisplayName()} in ${getSignature(parameters)}",
                    )
                }
            }.toMap()
    }

    fun getSignature(parameters: List<KParameter>): String = parameters
        .filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
        .joinToString(
            prefix = "(",
            postfix = ")",
        ) { parameter -> "${parameter.name}: ${parameter.type.toDisplayName()}" }

    fun decode(
        type: KType,
        element: JsonElement,
    ): Any? {
        if (element is JsonNull && type.isMarkedNullable) return null
        val serialized = runCatching { json.decodeFromJsonElement(json.serializersModule.serializer(type), element) }
        return serialized.getOrElse { serializationError ->
            runCatching {
                decodeReflectively(
                    type = type,
                    element = element,
                )
            }.getOrElse { reflectionError ->
                throw IllegalArgumentException(
                    "cannot read ${type.toDisplayName()} from $element: ${reflectionError.message ?: serializationError.message}",
                )
            }
        }
    }

    private fun decodeReflectively(
        type: KType,
        element: JsonElement,
    ): Any? {
        val kClass = requireNotNull(type.classifier as? KClass<*>) { "unsupported type ${type.toDisplayName()}" }
        return when {
            kClass.isSubclassOf(Collection::class) -> {
                val itemType = requireNotNull(type.arguments.firstOrNull()?.type) { "unknown item type" }
                val items = (element as JsonArray).map { item ->
                    decode(
                        type = itemType,
                        element = item,
                    )
                }
                if (kClass.isSubclassOf(Set::class)) items.toSet() else items
            }

            kClass.java.isEnum -> kClass.java.enumConstants.first { constant ->
                (constant as Enum<*>).name == element.jsonPrimitive.content
            }

            kClass.isSealed -> decodeSealed(
                kClass = kClass,
                element = element,
            )

            element is JsonObject -> build(
                kClass = kClass,
                arguments = element,
            )

            else -> throw IllegalArgumentException("expected a JSON object for ${kClass.simpleName}")
        }
    }

    private fun decodeSealed(
        kClass: KClass<*>,
        element: JsonElement,
    ): Any {
        val typeName = when (element) {
            is JsonPrimitive -> element.content

            is JsonObject -> requireNotNull(element[TYPE]?.jsonPrimitive?.content) {
                "${kClass.simpleName} is sealed: add \"$TYPE\" with one of ${kClass.findConcreteSubclasses().map {
                    it.simpleName
                }}"
            }

            else -> throw IllegalArgumentException("expected a subtype name or object for ${kClass.simpleName}")
        }
        val subclass = requireNotNull(kClass.findConcreteSubclasses().firstOrNull { it.simpleName == typeName }) {
            "$typeName is not one of ${kClass.findConcreteSubclasses().map { it.simpleName }}"
        }
        return build(
            kClass = subclass,
            arguments = JsonObject((element as? JsonObject).orEmpty() - TYPE),
        )
    }

    private companion object {
        const val TYPE = "@type"
    }
}

fun KClass<*>.findConcreteSubclasses(): List<KClass<*>> = if (isSealed) {
    sealedSubclasses.flatMap(KClass<*>::findConcreteSubclasses)
} else if (isAbstract || java.isInterface) {
    emptyList()
} else {
    listOf(this)
}
