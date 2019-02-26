package injection.api

@Target(AnnotationTarget.PROPERTY)
annotation class MapInject(val key: String = "")