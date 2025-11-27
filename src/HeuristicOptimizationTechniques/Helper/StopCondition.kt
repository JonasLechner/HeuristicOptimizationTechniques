package HeuristicOptimizationTechniques.Helper

sealed class StopCondition {
    data class Time(val maxSeconds: Int) : StopCondition()
    data class Iterations(val maxIterations: Int) : StopCondition()
}

class StopConditionGuard(private val stopCondition: StopCondition) {
    private val timeStarted: Long = System.currentTimeMillis();
    private var currentIteration: Long = 0

    fun shouldContinue(): Boolean {
        when (stopCondition) {
            is StopCondition.Iterations -> {
                currentIteration += 1
                return currentIteration <= stopCondition.maxIterations
            }

            is StopCondition.Time -> {
                val currentTime = System.currentTimeMillis()
                return (currentTime - timeStarted) / 1000 < stopCondition.maxSeconds
            }
        }
    }
}