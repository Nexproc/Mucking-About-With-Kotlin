package streaming.pipeline.info.broadcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.newCoroutineContext
import proto.InfoProto.Info
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@kotlinx.coroutines.ExperimentalCoroutinesApi
class InfoBroadcastChannel(
    channel: BroadcastChannel<Info> = BroadcastChannel(Channel.CONFLATED),
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext
) : BroadcastChannel<Info> by channel,
    CoroutineContext by scope.newCoroutineContext(context),
    CoroutineScope by scope