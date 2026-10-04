package com.quare.bibleplanner.tools.agentcli.session

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.tools.agentcli.json.ArgumentDecoder
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.json.findConcreteSubclasses
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/*
 * Why: a screen subscribes to every flow its ViewModel exposes, and many of them are stateIn with
 * WhileSubscribed, so the headless screen subscribes to all of them too or they never start.
 */
class HeadlessViewModel(
    val viewModel: ViewModel,
    private val scope: CoroutineScope,
    private val log: SessionLog,
    private val encoder: StateEncoder,
) {
    private val actionName = Regex("(?i)action|effect|command|message")
    private val ignoredFunctions = setOf("equals", "hashCode", "toString", "addCloseable", "getCloseable")
    private val kClass: KClass<out ViewModel> = viewModel::class
    val name: String = kClass.simpleName.orEmpty()
    private val values = ConcurrentHashMap<String, Any>()
    private val onEvent: KFunction<*>? = kClass.memberFunctions.firstOrNull { function ->
        function.name == ON_EVENT && function.visibility == KVisibility.PUBLIC && function.parameters.size == 2
    }
    val eventTypes: List<KClass<*>> = onEvent
        ?.parameters
        ?.last()
        ?.type
        ?.let(::resolve)
        ?.findConcreteSubclasses()
        .orEmpty()

    init {
        findPublicFlows().forEach { (propertyName, flow) ->
            when {
                flow is StateFlow<*> -> collectState(
                    propertyName = propertyName,
                    flow = flow,
                )

                flow is SharedFlow<*> || actionName.containsMatchIn(propertyName) -> collectActions(
                    propertyName = propertyName,
                    flow = flow,
                )

                else -> collectState(
                    propertyName = propertyName,
                    flow = flow,
                )
            }
        }
    }

    // Why: only the property a path asks for is encoded, unless it doesn't exist and the error should list them all.
    fun readState(
        maxItems: Int,
        onlyProperty: String?,
    ): JsonObject = JsonObject(
        values.entries
            .filter { (propertyName, _) ->
                onlyProperty == null || !values.containsKey(onlyProperty) || propertyName == onlyProperty
            }.sortedBy { (propertyName, _) -> propertyName }
            .associate { (propertyName, value) ->
                propertyName to encoder.encode(
                    value = value.takeUnless { it === NullValue },
                    maxItems = maxItems,
                )
            },
    )

    fun send(event: Any) {
        val function = requireNotNull(onEvent) { "$name has no onEvent" }
        function.isAccessible = true
        function.call(viewModel, event)
    }

    // Why: screens report their width class from the composition, which doesn't exist here.
    fun reportWidthClass(isWide: Boolean) {
        val eventType = eventTypes.firstOrNull { eventType -> eventType.simpleName == WIDTH_CLASS_CHANGED } ?: return
        val constructor = eventType.primaryConstructor ?: return
        constructor.isAccessible = true
        send(constructor.call(isWide))
    }

    suspend fun call(
        functionName: String,
        arguments: JsonObject,
        decoder: ArgumentDecoder,
        maxItems: Int,
    ): JsonElement {
        val candidates = kClass.memberFunctions.filter { function ->
            function.name == functionName && function.visibility == KVisibility.PUBLIC
        }
        val function = when (candidates.size) {
            1 -> candidates.single()
            0 -> throw IllegalArgumentException("$name has no public function $functionName")
            else -> throw IllegalArgumentException("$name.$functionName is overloaded")
        }
        val valueParameters = function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
        val bound = decoder.bind(
            function = function,
            parameters = valueParameters,
            arguments = arguments,
        )
        function.isAccessible = true
        val result = function.callSuspendBy(bound + (function.parameters.first() to viewModel))
        return encoder.encode(
            value = result.takeUnless { it == Unit },
            maxItems = maxItems,
        )
    }

    fun getFunctions(decoder: ArgumentDecoder): List<String> = kClass.memberFunctions
        .filter { function ->
            function.visibility == KVisibility.PUBLIC && function.name !in ignoredFunctions
        }.map { function ->
            function.name + decoder.getSignature(function.parameters)
        }.sorted()

    private fun collectState(
        propertyName: String,
        flow: Flow<*>,
    ) {
        scope.launch {
            flow.collect { value ->
                values[propertyName] = value ?: NullValue
                log.touch()
            }
        }
    }

    private fun collectActions(
        propertyName: String,
        flow: Flow<*>,
    ) {
        scope.launch {
            flow.collect { value ->
                log.record(
                    kind = LogKind.ACTION,
                    source = "$name.$propertyName",
                    payload = encoder.encode(
                        value = value,
                        maxItems = ACTION_MAX_ITEMS,
                    ),
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPublicFlows(): List<Pair<String, Flow<*>>> = kClass.memberProperties
        .filter { property ->
            property.visibility == KVisibility.PUBLIC &&
                (property.returnType.classifier as? KClass<*>)?.isSubclassOf(Flow::class) == true
        }.mapNotNull { property ->
            val flow = (property as KProperty1<Any, *>).get(viewModel) as? Flow<*>
            flow?.let { property.name to it }
        }

    private fun resolve(type: KType): KClass<*>? = when (val classifier = type.classifier) {
        is KClass<*> -> classifier

        is KTypeParameter -> kClass.allSupertypes.firstNotNullOfOrNull { supertype ->
            val owner = supertype.classifier as? KClass<*>
            val index = owner?.typeParameters?.indexOf(classifier) ?: -1
            if (index >= 0) supertype.arguments[index].type?.classifier as? KClass<*> else null
        }

        else -> null
    }

    // Why: a flow holding null still has to show up in the state, and ConcurrentHashMap can't hold null.
    private object NullValue

    private companion object {
        const val ON_EVENT = "onEvent"
        const val WIDTH_CLASS_CHANGED = "OnWidthClassChanged"
        const val ACTION_MAX_ITEMS = 20
    }
}
