package channels

import context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectClause2
import kotlin.coroutines.CoroutineContext

/**
 * [BroadcastChannel] implementation with size [Channel.UNLIMITED].  This [BroadcastChannel] delegates it's incoming
 * messages to a [Channel] of size [Channel.UNLIMITED] and consumes the elements directly from it.
 *
 * [kotlinx.coroutines] has plans to support a true [Channel.UNLIMITED] [BroadcastChannel] in a later release,
 */
@ExperimentalCoroutinesApi
class LinkedListBroadcastChannel<E> private constructor (
    private val coroutineContext: CoroutineContext,
    private val broadcastChannel: BroadcastChannel<E>,
    private val bufferChannel: Channel<E>
) : BroadcastChannel<E> by broadcastChannel, SendChannel<E> by bufferChannel {
    constructor(coroutineContext: CoroutineContext = Dispatchers.Default) : this(
        coroutineContext = coroutineContext,
        broadcastChannel = BroadcastChannel(1),
        bufferChannel = Channel(Channel.UNLIMITED)
    )

    companion object Factory {
        fun <E> new(coroutineContext: CoroutineContext = Dispatchers.Default) : BroadcastChannel<E> {
            val channel = LinkedListBroadcastChannel<E>(coroutineContext)
            channel.linkChannels()
            return channel
        }

        fun <E> CoroutineScope.newUnlimitedBroadcastChannel() = new<E>(context)
    }



    /** Manual delegation to [bufferChannel] for any element pushes. */
    override val onSend: SelectClause2<E, SendChannel<E>> = bufferChannel.onSend

    /** Manual delegation to [bufferChannel] for any element pushes. */
    override val isClosedForSend: Boolean = bufferChannel.isClosedForSend

    /** Manual delegation to [bufferChannel] for any element pushes. */
    override val isFull: Boolean =bufferChannel.isFull

    /** Manual delegation to [bufferChannel] for any element pushes. */
    override fun offer(element: E): Boolean = bufferChannel.offer(element)

    /** Manual delegation to [bufferChannel] for any element pushes. */
    override suspend fun send(element: E) = bufferChannel.send(element)

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        broadcastChannel.invokeOnClose(handler)
    }


    override fun close(cause: Throwable?): Boolean {
        broadcastChannel.close(cause)
        bufferChannel.close(cause)
        return true // by default, all internal channel impls seem to return true anyway
    }

    override fun cancel(cause: Throwable?): Boolean {
        bufferChannel.close(cause)
        broadcastChannel.cancel(cause)
        return true // by default, all internal channel impls seem to return true anyway
    }

    private fun linkChannels() {
        CoroutineScope(coroutineContext).launch {
            for (element in bufferChannel) broadcastChannel.send(element)
            broadcastChannel.close() // close when the stream ends, [bufferChannel] is closed.
        }
    }
}