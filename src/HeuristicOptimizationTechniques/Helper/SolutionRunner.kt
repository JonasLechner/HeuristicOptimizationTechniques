package HeuristicOptimizationTechniques.Helper

import HeuristicOptimizationTechniques.Algorithms.GRASP
import HeuristicOptimizationTechniques.Algorithms.ImprovementHeuristic
import HeuristicOptimizationTechniques.Algorithms.LocalSearch
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleMoveNeighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleSwapNeighborhood
import HeuristicOptimizationTechniques.Algorithms.NewGreedyConstruction
import HeuristicOptimizationTechniques.Algorithms.TabuSearch
import HeuristicOptimizationTechniques.Algorithms.VariableNeighborhoodDescent
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

class SolutionRunner() {
    companion object {
        private val basePath = File("instances/")
        val sizes = listOf("50", "100", "200", "500", "1000", "2000", "5000", "10000")
        private val logger = Logger.getLogger(SolutionRunner::class.java.simpleName)

        private val stopCondition = StopCondition.Iterations(50)

        private fun getLocals(neighborhood: Neighborhood): List<ImprovementHeuristic> =
            mutableListOf(
                LocalSearch(neighborhood, StepFunction.FIRST_IMPROVEMENT, stopCondition),
                LocalSearch(neighborhood, StepFunction.BEST_IMPROVEMENT, stopCondition),
            )

        private fun getAllLocals(instance: Instance): List<ImprovementHeuristic> {
            return getLocals(TwoSwapNeighborhood(instance)) +
                    getLocals(VehicleMoveNeighborhood(instance)) +
                    getLocals(VehicleSwapNeighborhood(instance))
        }

        fun run() {
            val builder = StringBuilder()
            builder.appendLine("Instance Size;Average Cost;Average Time")

            val builders = List(6) { StringBuilder() }
            builders.forEach { b -> b.appendLine("Instance Size;Average Cost;Average Time") }

            for (size in sizes) {
                var files = getFilePathsForInstance(size)

                //only do 5 for bigger sizes
                if (size in sizes.takeLast(4)) {
                    files = if (size in sizes.takeLast(2)) {
                        files.take(1)
                    } else {
                        files.take(5)
                    }
                }

                logger.info("Calculating instances with n=$size")

                var sumCost = 0.0
                var sumSeconds = 0.0

                val sumsCost = MutableList(6) { 0.0 }
                val sumstSeconds = MutableList(6) { 0.0 }

                for ((idx, file) in files.withIndex()) {
                    val inst = Instance(file.path)
                    val twentyPercent = max(500, inst.numberOfRequests / 5)
                    val onePercent = min(20, inst.numberOfRequests / 50)

                    val greedyConstruction = NewGreedyConstruction(inst, false, twentyPercent)
                    val localSearch = LocalSearch(
                        TwoSwapNeighborhood(inst),
                        StepFunction.BEST_IMPROVEMENT,
                        stopCondition
                    )

                    /*
                    val tabu = TabuSearch(
                        TwoSwapNeighborhood(inst),
                        stopCondition,
                        inst.numberOfRequests / 10
                    )

                    val grasp = GRASP(inst, TwoSwapNeighborhood(inst), StopCondition.Iterations(3))
                     */

                    val (solution2, seconds2) = heuristicTime {
                        greedyConstruction.construct()
                    }

                    val vnd = VariableNeighborhoodDescent(
                        listOf(
                            TwoSwapNeighborhood(inst),
                            VehicleMoveNeighborhood(inst)
                        )
                    )

                    val (solution, seconds) = heuristicTime {
                        vnd.improve(solution2)
                    }

                    /*
                    val locals = getAllLocals(inst)

                    for ((i, local) in locals.withIndex()) {
                        val (solution2, seconds2) = heuristicTime {
                            local.improve(solution)
                        }
                        sumsCost[i] += inst.computeObjectiveFunction(solution2.routes)
                        sumstSeconds[i] += seconds2
                    }

                     */
                    /*
                    val (solution2, seconds2) = heuristicTime {
                        localSearch.improve(solution)
                    }

                    println(
                        "before: ${inst.computeObjectiveFunction(solution.routes)}, after: ${
                            inst.computeObjectiveFunction(
                                solution2.routes
                            )
                        }"
                    )
                     */

                    /*
                    inst.writeSolution(
                        "results/averages/${file.name}",
                        solution.routes,
                        file.nameWithoutExtension
                    )
                     */

                    println("done ${idx + 1}/${files.size}")
                    sumSeconds += seconds
                    sumCost += inst.computeObjectiveFunction(solution.routes)
                }

                /*
                for (i in 0 until 6) {
                    val avgSeconds = "%.2f".format(sumstSeconds[i] / files.size)
                    val avgCost = "%.2f".format(sumsCost[i] / files.size)

                    val line = "$size;$avgCost;$avgSeconds"
                    builders[i].appendLine(line)
                }

                 */

                val avgSeconds = "%.2f".format(sumSeconds / files.size)
                val avgCost = "%.2f".format(sumCost / files.size)

                val line = "$size;$avgCost;$avgSeconds"
                builder.appendLine(line)
                println(line)

            }
            File("results/averages/tabu.csv").writeText(builder.toString())

            for (i in 0 until 6) {
                //File("results/averages/average_local_searched_$i.csv").writeText(builders[i].toString())
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