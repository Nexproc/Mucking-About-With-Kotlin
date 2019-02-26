package streaming.pipeline.info.delegate

import kotlinx.coroutines.channels.SendChannel
import proto.InfoProto
import streaming.pipeline.api.ChannelDelegate
import kotlin.coroutines.CoroutineContext

@kotlinx.coroutines.ExperimentalCoroutinesApi
class InfoChannelDelegate(
    initialValue: InfoProto.Info,
    override val channel: SendChannel<InfoProto.Info>, // delegate channel this property sends updates to
    parentContext: CoroutineContext
) : ChannelDelegate<InfoProto.Info>(initialValue, channel, parentContext) {
    override fun shouldClose(value: InfoProto.Info): Boolean {
        return (value.name == "RUN").also { if(it) println("YOU DON'T HAVE TO TELL ME TWICE") }
    }
}