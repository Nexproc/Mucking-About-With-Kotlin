package streaming.pipeline.info.receiver

import streaming.pipeline.info.broadcast.InfoBroadcastChannel
import kotlinx.coroutines.*
import proto.InfoProto.Info

@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class InfoReceiver(
    private val channel: InfoBroadcastChannel,
    private val exec: (Info) -> Unit = { Unit }
) {


    private val subscriber = channel.openSubscription()

    abstract fun processInfo(info: Info)

    suspend fun startAsync() = channel.async {
        println("Starting Info Processing")
        for (info in subscriber) {
            launch {
                delay(1000L)
                processInfo(info)
            }
        }
        with(this as Job) { children.forEach { it.join() } }
        println("Done Info Processing")
    }
}

@kotlinx.coroutines.ExperimentalCoroutinesApi
suspend fun InfoBroadcastChannel.newInfoReceiver(exec: (Info) -> Unit): Job {
    return object: InfoReceiver(this) {
        override fun processInfo(info: Info) = exec(info)
    }.startAsync()
}