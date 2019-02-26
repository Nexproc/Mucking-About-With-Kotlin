package streaming.pipeline.info.receiver

import streaming.pipeline.info.broadcast.InfoBroadcastChannel
import proto.InfoProto

@kotlinx.coroutines.ExperimentalCoroutinesApi
class PrintInfoReceiver(channel: InfoBroadcastChannel) : InfoReceiver(channel) {
    override fun processInfo(info: InfoProto.Info) {
        with(info) { println("ID: $id       NAME: $name") }
    }
}