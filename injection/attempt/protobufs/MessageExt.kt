package injection.attempt.protobufs

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Message.Builder
import com.google.protobuf.Message
import java.lang.IllegalArgumentException

/**
 * Populates [Message.Builder] fields from a [Map] of [String]s to the intended value on the proto.
 *
 * Still needs to other field types before it's ready to go -> namely oneofs.
 */
fun <T: Builder> T.inject(injectMap: InjectMap): T {
    val dmap = descriptorMap()
    this.apply { injectMap.forEach { (injectKey, injectValue) ->
        if(injectKey in dmap) {
            val field = dmap.getValue(injectKey)
            when {
                injectIfRepeated(field, injectValue) -> Unit
                injectIfMessage(field, injectValue) -> Unit
                else -> setField(field, injectValue)
            }
        }
    }}
    return this
}

private fun <T: Builder> T.injectIfRepeated(field: FieldDescriptor, injectVal: Any?): Boolean {
    if(field.isRepeated && injectVal is List<*>) {
        injectVal.forEach { value ->
            @Suppress("UNCHECKED_CAST")
            val objToAdd = when {
                value is Map<*, *> -> newBuilderForField(field).inject(value as InjectMap).build()
                value is Builder -> value.build()
                value is Message -> value
                field.isPackable -> value
                else -> IllegalArgumentException(
                    "Unable to convert class: ${value?.javaClass} to the expected type" +
                            "${field.messageType.name} for field ${field.name}.")
            }
            addRepeatedField(field, objToAdd)
        }
        return true
    }
    return false
}

private fun <T: Builder> T.injectIfMessage(field: FieldDescriptor, injectVal: Any?): Boolean {
    if(field.type == FieldDescriptor.Type.MESSAGE && injectVal is Map<*, *>) {
        @Suppress("UNCHECKED_CAST")
        getFieldBuilder(field).inject(injectVal as InjectMap)
        return true
    }
    return false
}


private fun <T: Builder> T.descriptorMap() : Map<String, FieldDescriptor> {
    return this.descriptorForType.fields.map {
        it.name to it
    }.toMap()
}

private typealias InjectMap = Map<String, Any?>