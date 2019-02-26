package streaming.pipeline.one

import streaming.pipeline.info.broadcast.InfoBroadcastChannel
import streaming.pipeline.info.receiver.PrintInfoReceiver
import streaming.pipeline.info.receiver.newInfoReceiver
import kotlinx.coroutines.*

@kotlinx.coroutines.ExperimentalCoroutinesApi
fun main() = runBlocking(CoroutineName("Main")) {
    val idToName = mapOf(
        1L to "Bob",
        2L to "Shirley",
        3L to "FRAGMEISTERCENTRAL",
        4L to "GERBPALOOPDUB",
        5L to "RUN"
    )

    val scope = this
    val broadcastChannel = InfoBroadcastChannel(scope = scope)
    broadcastChannel.newInfoReceiver { with(it) { println("ID: $id       NAME: $name") } }
    broadcastChannel.newInfoReceiver { with(it) { println("ID: $id       NAME: $name") } }
    broadcastChannel.newInfoReceiver { with(it) { println("ID: $id       NAME: $name") } }
    broadcastChannel.newInfoReceiver { with(it) { println("ID: $id       NAME: $name") } }
    broadcastChannel.newInfoReceiver { with(it) { println("NOID: $id       NONAME: $name") } }
    PrintInfoReceiver(broadcastChannel).startAsync()
    PrintInfoReceiver(broadcastChannel).startAsync()
    Runner(scope, broadcastChannel).doTheThing(idToName)
    println("Cancelling Children")
    with(this as Job) { children.forEach {
        it.join()
    }}
    this.coroutineContext.cancelChildren()
}