package injection.attempt.protobufs

import proto.InfoProto

fun main() {
    val infoMap = mapOf(
        "name" to "My Name!",
        "id" to 123,
        "info" to listOf(mapOf(
            "name" to "Nested!",
            "id" to 456
        ), mapOf(
            "name" to "Nested#2!",
            "id" to 789
        ))
    )
    val new = InfoProto.Info.newBuilder().inject(infoMap).build()
    println("Injected with: ID: ${new.id}       NAME: ${new.name}")
    new.infoList.forEach {nested ->
        println("Nested with: ID: ${nested.id}      NAME: ${nested.name}")
    }
}