import java.time.Duration

fun <K> MutableMap<K, Duration>.insertOrIncrement(key: K, value: Duration) {
    this[key] = this.getOrDefault(key, Duration.ZERO).plus(value)
}