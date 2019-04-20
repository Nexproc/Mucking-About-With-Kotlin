package channels

import TestClassSwapper
import addSubs
import channels.LinkedListBroadcastChannel.Factory.newUnlimitedBroadcastChannel
import kotlinx.coroutines.*
import log
import makeSubs
import reportFailures
import sendMessages
import waitForAllSends

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val subbers = makeSubs()
    coroutineScope {
        log("start")
        val channel = newUnlimitedBroadcastChannel<TestClassSwapper>()
        log("channels started")
        waitForAllSends(channel)
        log("waiter launcher")
        addSubs(channel, subbers)
        log("subscriptions launched")
        sendMessages(channel)
        log("message sends launched")
    }
    subbers.reportFailures()
}

