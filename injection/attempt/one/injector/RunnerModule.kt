package injection.attempt.one.injector

import injection.api.InjectableDelegate
import injection.attempt.one.`interface`.Runner
import injection.attempt.one.impl.RunnerImpl
import injection.attempt.one.impl.RunnerMapInjected

class RunnerModule(
    private val nameOverride: String = defaultName(),
    private val idOverride: Long = defaultID(),
    private val runnerOverride: Runner = defaultRunner(nameOverride, idOverride),
    private val mapRunnerOverride: Runner = defaultMapRunner(nameOverride, idOverride)
) {
    val runner: Runner
        get() = runnerOverride

    val mapRunner: Runner
        get() = mapRunnerOverride

    companion object {
        private fun defaultRunner(name: String, id: Long): Runner = InjectableDelegate.from {
            RunnerImpl(mapOf("myName" to name, "myId" to id))
        }

        private fun defaultMapRunner(name: String, id: Long): Runner = InjectableDelegate.from {
            RunnerMapInjected(mapOf("name" to name, "id" to id))
        }

        private fun defaultName(): String = InjectableDelegate.from("My Name")

        private fun defaultID(): Long = InjectableDelegate.singleton(123L)
    }
}