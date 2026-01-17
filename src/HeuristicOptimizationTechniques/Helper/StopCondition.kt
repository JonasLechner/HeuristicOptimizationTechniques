package HeuristicOptimizationTechniques.Helper

sealed class StopCondition {
    data class Time(val maxSeconds: Int) : StopCondition()
    data class Iterations(val maxIterations: Int) : StopCondition()
    data class NoImprovementForKIterations(val maxIterationsNoChange: Int) : StopCondition()
}

class CombinedStopConditionGuard(conditions: List<StopCondition>) {

    private val guards: List<StopConditionGuard> = conditions.map { StopConditionGuard(it) }

    fun shouldContinue(objectiveValue: Double? = null): Boolean {
        return guards.all { it.shouldContinue(objectiveValue) }
    }
}

class StopConditionGuard(private val stopCondition: StopCondition) {
    private val timeStarted: Long = System.currentTimeMillis()
    private var currentIteration: Long = 0
    private var bestObj: Double = Double.MAX_VALUE
    private var iterationsUnchanged: Int = 0

    fun shouldContinue(objectiveValue: Double? = null): Boolean {
        return when (stopCondition) {
            is StopCondition.Iterations -> {
                currentIteration += 1
                currentIteration <= stopCondition.maxIterations
            }

            is StopCondition.Time -> {
                val currentTime = System.currentTimeMillis()
                (currentTime - timeStarted) / 1000 < stopCondition.maxSeconds
            }

            is StopCondition.NoImprovementForKIterations -> {
                // NOTE: no iteration cap here anymore; that's handled by StopCondition.Iterations if present.
                require(objectiveValue != null) {
                    "objectiveValue must be provided when using NoImprovementForKIterations"
                }

                if (objectiveValue >= bestObj) {
                    iterationsUnchanged += 1
                } else {
                    bestObj = objectiveValue
                    iterationsUnchanged = 0
                }

                iterationsUnchanged < stopCondition.maxIterationsNoChange
            }
        }
    }
}
