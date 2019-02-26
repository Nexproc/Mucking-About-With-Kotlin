package injection.api

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

interface InjectableDelegate<T>: ReadOnlyProperty<Any?, T> {
    companion object Factory {
        fun <I> from(default: I): I {
            val ret by FactoryInjectable { default }
            return ret
        }

        fun <I> from(factory: () -> I): I {
            val ret by FactoryInjectable(factory)
            return ret
        }

        fun <I> singleton(impl: I): I {
            val ret by SingletonInjectable(impl)
            return ret
        }
    }
}

private class SingletonInjectable<T>(val default: T): InjectableDelegate<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return default
    }
}

private class FactoryInjectable<T>(val factory: () -> T): InjectableDelegate<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return factory()
    }
}

abstract class Injectable<T>(injectMap: InjectMap) {
    init { inject(injectMap) }
}

private fun <T> Injectable<T>.inject(injectMap: InjectMap) {
    println("injecting")
    this::class.memberProperties.forEach{prop ->
        prop.willBeInjected(injectMap)?.let { key ->
            println("Property: ${prop.name} will be injected from map!")
            prop as MInjectProp<T>
            prop.internalInject(this, injectMap, key)
            println("Property: ${prop.name} injected!")
        }
    }
    println("Done injecting")
}

private fun KProperty<*>.willBeInjected(injectMap: InjectMap): String? {
    val injectPlzKey = this.findAnnotation<MapInject>()?.key
    if (injectPlzKey != null) {
        return when {
            injectPlzKey.isNotBlank() && injectPlzKey in injectMap -> injectPlzKey
            injectPlzKey.isBlank() && name in injectMap -> name
            else -> null
        }
    }
    return null
}

private fun <T> MInjectProp<T>.internalInject(ref: Any?, injectMap: InjectMap, key: String) {
    if(returnType.isMarkedNullable) setter.call(ref, injectMap[key])
    else setter.call(ref, injectMap.getValue(key))
}

private typealias InjectMap = Map<String, Any?>
private typealias MInjectProp<T> = KMutableProperty1<out Injectable<T>, *>