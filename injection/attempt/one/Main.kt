package injection.attempt.one

import injection.attempt.one.injector.RunnerModule

fun main() {
    val module = RunnerModule()
    println("module created")
    val runner = module.runner
    println("regular runner created")
    val mapRunner = module.mapRunner
    println("map runner created")
    val ret = runner.run()
    println("Regular Runner Ran, Result: Name: ${ret.name}      ID: ${ret.id}")
    val mappedRet = mapRunner.run()
    println("Mapped Runner Ran, Result:  Name: ${mappedRet.name}      ID: ${mappedRet.id}")
}