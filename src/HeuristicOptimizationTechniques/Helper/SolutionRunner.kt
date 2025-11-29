package HeuristicOptimizationTechniques.Helper

import HeuristicOptimizationTechniques.Algorithms.ConstructionHeuristic
import HeuristicOptimizationTechniques.Algorithms.PilotSearch
import java.io.File
import kotlin.system.measureTimeMillis

class SolutionRunner() {
    companion object {
        private val basePath = File("instances/")
        val sizes = listOf("50", "100", "200", "500", "1000", "2000", "5000", "10000")

        private val logger = Logger.getLogger(SolutionRunner::class.java.simpleName)

        fun run() {
            for (size in sizes.take(6)) {
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
                    val pilotSearch = PilotSearch(inst, 3, 3)
                    val (solution, seconds) = runConstructionHeuristic(pilotSearch, inst)

                    val realCost = inst.computeObjectiveFunction(solution.routes).toInt()
                    builder.appendLine(
                        "${file.nameWithoutExtension};$realCost;${
                            "%.2f".format(
                                seconds
                            )
                        }"
                    )
                }
                File("results/pilot/n_${size}_solutions.csv").writeText(builder.toString())
            }
        }

        fun runConstructionHeuristic(
            heuristic: ConstructionHeuristic,
            instance: Instance
        ): Pair<Solution, Double> {
            val solution: Solution?
            val time = measureTimeMillis {
                solution = heuristic.construct()
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