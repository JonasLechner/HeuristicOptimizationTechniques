package HeuristicOptimizationTechniques.Helper

import HeuristicOptimizationTechniques.Algorithms.CompletelyRandomConstruction
import HeuristicOptimizationTechniques.Algorithms.GeneticAlgorithm
import HeuristicOptimizationTechniques.Helper.StopCondition.NoImprovementForKIterations
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val args = parseGeneticArgs(args)

    val instance = Instance(args.instancePath)

    var output = ""
    val time = measureTimeMillis {
        val random = Random(args.seed)
        val geneticAlgorithm = GeneticAlgorithm( //new NewGreedyConstruction(i2, true, 5),
            CompletelyRandomConstruction(instance, random),
            instance,
            args.populationSize,
            args.eliteCount,
            random,
            listOf(
                NoImprovementForKIterations(args.iterationsNoChange),
                StopCondition.Time(args.seconds - 1)
            ),
            args.mutationRate
        )

        output += (geneticAlgorithm.construct().totalCost)
    }

    output += " " + time / 1000

    println(output)
}

data class GeneticCliConfig(
    val instancePath: String,
    val seed: Long,
    val populationSize: Int,
    val eliteCount: Int,
    val mutationRate: Double,
    val iterationsNoChange: Int,
    val seconds: Int,
)

fun parseGeneticArgs(args: Array<String>): GeneticCliConfig {
    // Expected exact order:
    // 0: path
    // 1: seed
    // 2: --populationSize  3: value
    // 4: --eliteCount      5: value
    // 6: --mutationRate    7: value
    // 8: --iterationsNoChange 9: value
    if (args.size != 11) throw IllegalArgumentException("Expected 11 args, got ${args.size}: ${args.joinToString(", ")}")

    if (args[3] != "--populationSize") throw IllegalArgumentException("Expected --populationSize at args[2], got '${args[2]}'")
    if (args[5] != "--eliteCount") throw IllegalArgumentException("Expected --eliteCount at args[4], got '${args[4]}'")
    if (args[7] != "--mutationRate") throw IllegalArgumentException("Expected --mutationRate at args[6], got '${args[6]}'")
    if (args[9] != "--iterationsNoChange") throw IllegalArgumentException("Expected --iterationsNoChange at args[8], got '${args[8]}'")

    return GeneticCliConfig(
        instancePath = args[0],
        seed = args[1].toLong(),
        populationSize = args[4].toInt(),
        eliteCount = args[6].toInt(),
        mutationRate = args[8].toDouble(),
        iterationsNoChange = args[10].toInt(),
        seconds = args[2].toInt()
    )
}
