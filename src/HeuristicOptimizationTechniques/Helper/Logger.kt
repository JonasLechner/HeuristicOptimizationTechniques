package HeuristicOptimizationTechniques.Helper

class Logger(private val tag: String) {

    fun debug(message: String) = log("DEBUG", message)
    fun info(message: String) = log("INFO", message)
    fun warn(message: String) = log("WARN", message)
    fun error(message: String, throwable: Throwable? = null) = log("ERROR", message, throwable)

    private fun log(level: String, message: String, throwable: Throwable? = null) {
        val timestamp = java.time.LocalDateTime.now()
        println("[${formattedTimestamp()}] [$level] [$tag] $message")
        throwable?.printStackTrace()
    }

    private fun formattedTimestamp(): String =
        java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))


    companion object {
        fun getLogger(tag: String) = Logger(tag)
    }
}
