package streaming.pipeline.one

import streaming.pipeline.info.delegate.InfoChannelDelegate
import streaming.pipeline.info.broadcast.InfoBroadcastChannel
import kotlinx.coroutines.*
import proto.InfoProto.Info

@kotlinx.coroutines.ExperimentalCoroutinesApi
class Runner(
    val scope: CoroutineScope,
    val channel: InfoBroadcastChannel
) {
    private val channelDelegate = InfoChannelDelegate(
        Info.getDefaultInstance(),
        channel,
        scope.coroutineContext
    )
    private var recentInfo: Info by channelDelegate

    fun doTheThing(idToName: Map<Long, String>) = runBlocking(CoroutineName("Do The Thing")) {
        idToName.toList().mapIndexed{ i, (setId, setName) ->
            scope.async(CoroutineName("AsyncInfoBuilder")) {
                delay(100L * i)
                Info.newBuilder().apply {
                    id = setId
                    name = setName
                }.build()
            }
        }.forEach {
            it.join()
            recentInfo = it.getCompleted()
            println("Completed Processing of - ID:${recentInfo.id}        Name: ${recentInfo.name}")
        }
        channelDelegate.close()
        channel.close()
    }
}