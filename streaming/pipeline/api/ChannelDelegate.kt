package streaming.pipeline.api

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty
import kotlin.coroutines.CoroutineContext

/** Property delegate that broadcasts async updates to another [SendChannel] when its value changes. */
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class ChannelDelegate<T>(
    initialValue: T,
    override val channel: SendChannel<T>, // delegate channel this property sends updates to
    parentContext: CoroutineContext
) : ObservableProperty<T>(initialValue), ProducerScope<T>, SendChannel<T> by channel {
    override val coroutineContext: CoroutineContext = parentContext
    val jobs = mutableListOf<Job>()

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        println("launching")
        jobs.add(async(CoroutineName("AfterChange")) {
            println("launched!")
            channel.send(newValue)
        })

        if(shouldClose(newValue)) {
            runBlocking(CoroutineName("After Change - Should Close")) {
                jobs.joinAll()
                close()
            }
        }
    }

    abstract fun shouldClose(value: T): Boolean
}
