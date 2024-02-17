import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource


interface RateLimiter {
    fun hitApi(): Boolean
    val apiLimitCount: Int
}

@OptIn(ExperimentalTime::class)
class KotlinRateLimiter(override val apiLimitCount: Int) : RateLimiter {

    private val timeSource = TimeSource.Monotonic
    private var mark1 = timeSource.markNow()
    private var oneMinute: Duration = 0.2.minutes
    private var mark2 = mark1 + oneMinute
    private var currentCount = 0

    private fun resetMarksAndDuration() {
        mark1 = timeSource.markNow()
        mark2 = mark1 + oneMinute
    }

    override fun hitApi(): Boolean {
        var isAllowed = false
        println(currentCount++)
        if (mark2.hasNotPassedNow() && currentCount <= apiLimitCount) {
            isAllowed = true
        } else if (mark2.hasPassedNow()) {
            currentCount = 0
            isAllowed = false
            resetMarksAndDuration()
        }
        return isAllowed
    }
}

suspend fun main() {
    val limiter = KotlinRateLimiter(2)
    repeat(19000000) {
        println(limiter.hitApi())
        delay(1000)
    }
}