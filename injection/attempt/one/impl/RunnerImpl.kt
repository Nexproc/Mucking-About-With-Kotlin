package injection.attempt.one.impl

import injection.api.MapInject
import injection.api.Injectable
import injection.attempt.one.`interface`.Runner
import proto.InfoProto.Info

class RunnerImpl(injectMap: Map<String, Any>): Runner {
    val myName: String by injectMap
    val myId: Long by injectMap

    override fun run(): Info {
        return Info.newBuilder().apply{
            name = myName
            id = myId
        }.build()
    }
}

class RunnerMapInjected(injectMap: Map<String, Any?>) : Runner, Injectable<Runner>(injectMap) {
    @MapInject("name")
    lateinit var runName: String

    @MapInject("id")
    lateinit var runId: Number

    override fun run(): Info {
        return Info.newBuilder().apply{
            name = runName
            id = runId.toLong()
        }.build()
    }
}