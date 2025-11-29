package HeuristicOptimizationTechniques.Helper

import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction
import HeuristicOptimizationTechniques.Algorithms.LocalSearch
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TestNeighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleMoveNeighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleSwapNeighborhood
import HeuristicOptimizationTechniques.Algorithms.PilotSearch
import HeuristicOptimizationTechniques.Algorithms.RandomizedConstruction
import HeuristicOptimizationTechniques.Algorithms.VariableNeighborhoodDescent
import java.io.File
import kotlin.system.measureTimeMillis

class SolutionRunner() {
    companion object {
        private val basePath = File("instances/")
        val sizes = listOf("50", "100", "200", "500", "1000", "2000", "5000", "10000")

        private val logger = Logger.getLogger(SolutionRunner::class.java.simpleName)

        fun run() {
            for (size in sizes.take(5).takeLast(1)) {
                var files = getFilePathsForInstance(size)

                //only do 5 for bigger sizes
                if (size in sizes.takeLast(4)) {
                    files = files.take(5)
                }

                val builder = StringBuilder()
                builder.appendLine("Instance;Cost;Time (s)")

                logger.info("Calculating instances with n=$size")
                for (file in files) {
                    val inst = Instance(file.path)
                    val pilotSearch = PilotSearch(inst, 10, 5)
                    val greedyConstruction = RandomizedConstruction(inst, 5, 10)

                    val vnd = VariableNeighborhoodDescent(
                        listOf(
                            TestNeighborhood(inst, 3),
                            TwoSwapNeighborhood(inst),
                        )
                    )

                    val localSearch = LocalSearch(
                        TwoSwapNeighborhood(inst),
                        StepFunction.BEST_IMPROVEMENT,
                        StopCondition.Iterations(100)
                    )
                    val (solution, seconds) = heuristicTime {
                        greedyConstruction.construct()
                    }

                    val (solution2, seconds2) = heuristicTime {
                        vnd.improve(solution)
                    }

                    println("Pilot: ${inst.computeObjectiveFunction(solution.routes)}, $seconds")
                    println("Pilot Localsearched: ${inst.computeObjectiveFunction(solution2.routes)}, $seconds2")

                    /*
                    builder.appendLine(
                        "${file.nameWithoutExtension};$realCost;${
                            "%.2f".format(
                                seconds
                            )
                        }"
                    )
                     */
                }
                //File("results/pilot/n_${size}_solutions.csv").writeText(builder.toString())
            }
        }

        fun heuristicTime(
            heuristicF: () -> Solution,
        ): Pair<Solution, Double> {
            val solution: Solution?
            val time = measureTimeMillis {
                solution = heuristicF()
            }

            if (solution == null) {
                throw Error("Could not find a solution")
            }

            return Pair(solution, time / 1000.0)
        }

        fun getFilePathsForInstance(instance: String): List<File> {
            val folder = basePath.resolve(instance).resolve("test")
            val allFiles = folder.listFiles() ?: emptyArray()

            return allFiles.toList()
        }
    }
}