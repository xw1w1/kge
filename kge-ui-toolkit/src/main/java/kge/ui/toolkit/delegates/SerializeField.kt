package kge.ui.toolkit.delegates

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SerializeField(
    val label: String = ""
)
