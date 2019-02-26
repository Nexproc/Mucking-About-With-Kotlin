import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.whileSelect
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger
import kotlin.random.Random

typealias TestClassSwapper = BarClass

const val SENDS = 10000
const val SUBS = 1000
val logger = Logger.getLogger("testing")
val SEND_SET = SENDS.toSendSet()

fun makeSubs(): List<Subscriber> {
    val subbers = mutableListOf<Subscriber>()
    repeat(SUBS) {
        subbers.add(Subscriber())
    }
    return subbers
}

fun CoroutineScope.log(msg: String) {
    logger.info("$context: $msg")
}

val CoroutineScope.context
    get() = coroutineContext

@ExperimentalCoroutinesApi
fun addSubs(broadcaster: BroadcastChannel<TestClassSwapper>, subbers: List<Subscriber>): List<Job> {
    return subbers.mapIndexed { idx, sub ->
        val channelIter = broadcaster.openSubscription()
        return@mapIndexed defaultLaunch("Subscriber #$idx") {
            for(x in channelIter) sub.onReceive(x)
        }
    }
}

@ExperimentalCoroutinesApi
fun <T> waitForAllSends(channel: BroadcastChannel<T>) {
    val sends = AtomicInteger(0)
    val watcherSub = channel.openSubscription()
    defaultLaunch {
        whileSelect { watcherSub.onReceive { sends.incrementAndGet() < SENDS } }
        channel.close()
    }
}

fun List<Subscriber>.reportFailures() {
    log("report start")
    forEach { it.report() }
    log("report end")
    val failed = filter { it.failure.isFailed }.map { it.failure }

    if (failed.isEmpty()) return log("Job's Done, No failures")

    log("Subscribers Failed: ${failed.size}")
    log("Avg Messages Missed: ${failed.sumBy { it.missedCount } / (failed.size + 0.0)}")
    val failureMap = mutableMapOf<Int, Int>()
    failed.forEach {
        it.missingValues.forEach { missed ->
            failureMap[missed.number] = failureMap.getOrDefault(missed.number, 0) + 1
        }
    }
    val topFails = failureMap.map { (k, v) -> v to k }.toMap().toSortedMap().subMap(0, 3).toList()
        .fold("") { a, (v, k) -> "$a, $k: $v" }
    log("Most Missed Messages: $topFails")
}


fun sendMessages(channel: SendChannel<TestClassSwapper>): List<Job> {
    val rand = Random(SENDS)
    return SEND_SET.mapIndexed { idx, it ->
        defaultLaunch("Send #$idx") {
            if (!channel.offer(it)) channel.send(it)
        }
    }
}

class Subscriber {
    data class Failure(
        val isFailed: Boolean,
        val missedCount: Int = 0,
        val missingValues: List<TestClassSwapper> = emptyList()
    )

    var failure = Failure(false)
        private set(value) {
            field = value
        }

    private val msgQ = ConcurrentLinkedQueue<TestClassSwapper>()
    private val messagesReceived = AtomicInteger(0)
    fun onReceive(x: TestClassSwapper) {
        msgQ.add(x)
        messagesReceived.incrementAndGet()
    }

    fun report() {
        val total = messagesReceived.get()
        if (total != SENDS) {
            msgQ.toSet()
            failure = Failure(
                isFailed = true,
                missedCount = SENDS - total,
                missingValues = SEND_SET.filterNot { it in msgQ }
            )

            log("Expected: $SENDS, WAS: $total")

        }
    }
}

fun log(msg: String) {
    logger.info(msg)
}

fun defaultLaunch(
    name: String = "Anonymous Launch",
    block: suspend CoroutineScope.() -> Unit
): Job {
    val defaultScope = CoroutineScope(Dispatchers.Default)
    return defaultScope.launch(defaultScope.context + CoroutineName(name)) { block() }
}

class BarClass(val number: Int) {
    fun foo() {
        log("Cool")
    }
}

fun Int.toSendSet(): Set<TestClassSwapper> {
    return downTo(1).map { BarClass(it - 1) }.toSet()
}