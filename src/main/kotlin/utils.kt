
import RetryStrategy.Companion.RETRY
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.base.MoreObjects
import com.google.common.io.Files
import com.google.common.io.Resources
import com.google.gson.Gson
import com.rits.cloning.Cloner
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.lang.RuntimeException
import java.lang.Thread.sleep
import java.lang.reflect.Type
import java.math.BigDecimal
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.text.Charsets.UTF_8

private val cloner = Cloner()
private val gson = Gson()
val random = Random()

// Common

inline fun <reified T : Any> logger(): Logger = LoggerFactory.getLogger(T::class.java)
fun generateGuid(): String = UUID.randomUUID().toString().replace("-".toRegex(), "")

// Serialization/deserialization

private val objectMapper: ObjectMapper by lazy {
    ObjectMapper()
            .registerModule(KotlinModule())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
//            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .registerModule(KotlinModule())
//            .registerModule(JsonOrgModule())
}

fun Any.toJSON(): String {
    try {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    } catch (e: JsonProcessingException) {
        throw RuntimeException(e)
    }
}

fun <T> String.fromJSON(typeReference: TypeReference<T>): T {
    try {
        return objectMapper.readValue<T>(this, typeReference)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}

fun <T> fromJSON(json: String, clazz: Class<T>): T {
    try {
        return objectMapper.readValue(json, clazz)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }

}

fun toGSON(o: Any): String { //todo ext
    return gson.toJson(o)
}

fun <T> fromGSON(json: String, clazz: Class<T>): T { //todo ext
    return gson.fromJson(json, clazz)
}

/**
 * Usage: new Type<Token>(){}.getType()
 */
fun <T> fromGSON(json: String, type: Type): T { //todo ext
    return gson.fromJson<T>(json, type)
}

// Cloning

fun <T : Any> deepClone(obj: T): T {
    return cloner.deepClone(obj)
}


// File input/output

fun <T> load(path: String, typeReference: TypeReference<T>): T {
    val contents = Files.toString(File(path), Charsets.UTF_8)
    return contents.fromJSON(typeReference)
}

inline fun <reified T> load(path: String): T {
    val contents = Files.toString(File(path), Charsets.UTF_8)
    return contents.fromJSON(object : TypeReference<T>() {})
}

fun <T : Any> save(obj: T, path: String) {
    Files.write(obj.toJSON(), File(path), Charsets.UTF_8)
}

// Resources

fun getUTF8Resource(resourceName: String): String {
    return Resources.toString(Resources.getResource(resourceName), com.google.common.base.Charsets.UTF_8)
}

fun getResourcesForDir(dir: String): Array<out Resource> {
    val resources: Array<out Resource>? = PathMatchingResourcePatternResolver().getResources("classpath:$dir/*.*")
    return resources ?: emptyArray()
}

// Images

fun arrayToImage(activations: Array<IntArray>): BufferedImage {
    if (activations.size == 0 || activations[0].size == 0) {
        throw RuntimeException()
    }

    val iArray = intArrayOf(0, 0, 0, 255)  //  pixel

    val height = activations.size
    val width = activations[0].size
    val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val raster = image.raster
    for (row in 0..height - 1) {
        for (col in 0..width - 1) {
            val value = activations[row][col]
            iArray[0] = value
            iArray[1] = value
            iArray[2] = value
            raster.setPixel(col, row, iArray)
        }
    }
    return image
}

fun imageToArray(image: BufferedImage): Array<IntArray> {
    val width = image.width
    val height = image.height

    if (height == 0 || width == 0) {
        throw RuntimeException()
    }

    val res = Array(height) { IntArray(width) }
    for (i in 0..height - 1) {
        for (j in 0..width - 1) {
            val rgb = image.getRGB(j, i)
            res[i][j] = rgb and 0xFF
        }
    }
    return res
}

// Constructs

fun <T> tryOrFail(function: () -> T): T {
    try {
        return function()
    } catch (e: Throwable) {
        throw RuntimeException(e)
    }
}

fun tryNotFail(function: () -> Unit) {
    try {
        return function()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun <T> tryOrNull(function: () -> T): T? {
    try {
        return function()
    } catch (e: Throwable) {
        return null
    }
}

fun <T> tryOrElse(value: T, function: () -> T): T {
    try {
        return function()
    } catch (e: Throwable) {
        return value
    }
}

fun notNulls(vararg args: Any?) = args.none { it == null }

fun <T1> let(t1: T1?, f: (T1) -> Unit) = {
    if (t1 != null) f(t1)
}

fun <T1, T2> let(t1: T1?, t2: T2?, f: (T1, T2) -> Unit) = {
    if (t1 != null && t2 != null) f(t1, t2)
}

fun <T1, T2, T3> let(t1: T1?, t2: T2?, t3: T3?, f: (T1, T2, T3) -> Unit) = {
    if (t1 != null && t2 != null && t3 != null) f(t1, t2, t3)
}

fun <T1, T2, T3, T4> let(t1: T1?, t2: T2?, t3: T3?, t4: T4?, f: (T1, T2, T3, T4) -> Unit) = {
    if (t1 != null && t2 != null && t3 != null && t4 != null) f(t1, t2, t3, t4)
}

fun <T1, T2, T3, T4, T5> let(t1: T1?, t2: T2?, t3: T3?, t4: T4?, t5: T5?, f: (T1, T2, T3, T4, T5) -> Unit) = {
    if (t1 != null && t2 != null && t3 != null && t4 != null && t5 != null) f(t1, t2, t3, t4, t5)
}


inline fun <T> doFor(vararg elements: T, function: (T) -> Unit) {
    for (element in elements) function(element)
}

fun <T> repeatWith(initial: T, count: Int, block: (T) -> T): T {
    var temp: T = initial
    repeat(count) {
        temp = block(temp)
    }
    return temp
}


/**
Конструкция для того, чтобы сделать что-нибудь перед тем, как возвратиться из функции, по причине того, что аргумент == null
nullableArg ?: nil { ... } ?: return
 */
fun <T> nil(func: () -> Unit): T? {
    func()
    return null
}

fun <T> nullableIf(condition: Boolean?, trueVariant: T, falseVariant: T): T? {
    return if (condition != null) {
        if (condition) trueVariant else falseVariant
    } else null
}

fun <T> nullableIf(condition: Boolean?, trueVariant: T, falseVariant: T, nullVariant: T): T {
    return if (condition != null) {
        if (condition) trueVariant else falseVariant
    } else nullVariant
}

fun probableTrue(probability: Int): Boolean {
    return random.nextInt(100) < probability
}

class MapBuilder {
    val map = HashMap<String, Any?>()

    infix fun String.to(value: Any?) {
        map[this] = value
    }
}

inline fun <T> T.myStr(init: MapBuilder.() -> Unit): String = MoreObjects.toStringHelper(this).omitNullValues().apply {
    MapBuilder().apply { init() }.map.forEach {
        add(it.key, it.value)
    }
}.toString()


infix fun <T> Int.fitIn(list: List<T>): Boolean = 0 <= this && this < list.size
infix fun Int.fitIn(count: Int): Boolean = 0 <= this && this < count
infix fun Int.fitIn(count: Long): Boolean = 0 <= this && this < count
fun fill(value: Any?) = value?.toString() ?: ""
val Int.ms: Int get() = this
val Int.sec: Int get() = this * 1000
val Int.min: Int get() = this * 60 * 1000
val Int.h: Int get() = this * 60 * 60 * 1000

fun nullIfEmpty(s: String?) = if (s != null && s.isEmpty()) null else s

// Collections

fun <T> List<T>.breadthSearch(startIndex: Int? = null, filter: (T) -> Boolean): T? {
    val maxIndex = this.size - 1

    val startWith = when {
        startIndex == null -> 0
        startIndex > maxIndex -> maxIndex
        startIndex < 0 -> 0
        else -> startIndex
    }

    var curLeftIndex = startWith
    var curRightIndex = startWith

    // проверить стартовую точку один раз
    //    println("checking start $startWith")
    if (filter(this[startWith])) {
        return this[startWith]
    }

    // ищем в ширину
    while (true) {

        if (curLeftIndex > 0) {
            curLeftIndex--
            //            println("checking left $curLeftIndex")
            val t = this[curLeftIndex]
            if (filter(t)) {
                return t
            }
        }

        if (curRightIndex < maxIndex) {
            curRightIndex++
            //            println("checking right $curRightIndex")
            val t = this[curRightIndex]
            if (filter(t)) {
                return t
            }
        }

        if (curLeftIndex <= 0 && curRightIndex >= maxIndex) break
    }

    return null
}

fun <T> List<T>.breadthSearchWithIndex(startIndex: Int? = null, filter: (T) -> Boolean): IndexedValue<T>? {
    val maxIndex = this.size - 1

    val startWith = when {
        startIndex == null -> 0
        startIndex > maxIndex -> maxIndex
        startIndex < 0 -> 0
        else -> startIndex
    }

    var curLeftIndex = startWith
    var curRightIndex = startWith

    // проверить стартовую точку один раз
    //    println("checking start $startWith")
    if (filter(this[startWith])) {
        return IndexedValue(startWith, this[startWith])
    }

    // ищем в ширину
    while (true) {

        if (curLeftIndex > 0) {
            curLeftIndex--
            //            println("checking left $curLeftIndex")
            val t = this[curLeftIndex]
            if (filter(t)) {
                return IndexedValue(curLeftIndex, t)
            }
        }

        if (curRightIndex < maxIndex) {
            curRightIndex++
            //            println("checking right $curRightIndex")
            val t = this[curRightIndex]
            if (filter(t)) {
                return IndexedValue(curRightIndex, t)
            }
        }

        if (curLeftIndex <= 0 && curRightIndex >= maxIndex) break
    }

    return null
}

// Properties and delegates

inline fun <T> observer(initialValue: T, crossinline onSet: (newValue: T) -> Unit): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onSet(newValue)
}

inline fun <T> changeObserver(initialValue: T, crossinline onChange: (newValue: T) -> Unit): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        if (oldValue != newValue) onChange(newValue)
    }
}

inline fun <T> transitionObserver(initialValue: T, crossinline onTransition: (oldValue: T, newValue: T) -> Unit): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        if (oldValue != newValue) onTransition(oldValue, newValue)
    }
}

// Misc

fun <T> getListSlice(list: List<T>, from: Int, count: Int): List<T> {
    return if (from < 0 || from >= list.size)
        emptyList()
    else if (from + count > list.size)
        list.subList(from, list.size)
    else
        list.subList(from, from + count)
}

fun convertInputStreamToString(inputStream: InputStream?): String? {
    return if (inputStream != null) {
        val writer = StringWriter()
        IOUtils.copy(inputStream, writer, UTF_8)
        inputStream.close()
        writer.toString()

    } else {
        null
    }
}

class RetryStrategy(
        val retry: Boolean = true,
        val delayMs: Long = 1_000,
        val maxAttempts: Int = 3,
        val increasingInterval: Boolean = false
) {
    companion object {
        val DONT_RETRY = RetryStrategy(retry = false, delayMs = 0, maxAttempts = 0, increasingInterval = false)
        val RETRY = RetryStrategy(retry = true, delayMs = 3_000, maxAttempts = 3, increasingInterval = true)
        val LONG_RETRY = RetryStrategy(retry = true, delayMs = 5_000, maxAttempts = 10, increasingInterval = true)
        val DEFAULT_RETRY = DONT_RETRY
    }
}

fun <T> retryOnError(retry: Boolean = true, delayMs: Long = 1_000, maxAttempts: Int = 3, increasingInterval: Boolean = false, function: () -> T): T {
    return retryOnError(RetryStrategy(retry, delayMs, maxAttempts, increasingInterval), function)
}

fun <T> retryOnError(retryStrategy: RetryStrategy = RETRY, function: () -> T): T {
    var tryOneMoreTime = false
    var res: T? = null
    var attemptsCount = 0
    var currentDelay: Long = retryStrategy.delayMs

    fun increaseDelaysIfNeeded() {
        if (retryStrategy.increasingInterval) {
            currentDelay *= 2
        }
    }

    do {
        try {
            attemptsCount++
            res = function.invoke()
            tryOneMoreTime = false

        } catch (e: Throwable) {
            with(retryStrategy) {
                tryOneMoreTime = retry
                if (tryOneMoreTime && attemptsCount < maxAttempts) {
//                    println("Retry scheduled due to error (${e.message}). sleeping for $currentDelay")
                    sleep(currentDelay)
                    increaseDelaysIfNeeded()
                } else throw e
            }
        }
    } while (tryOneMoreTime)

    return res ?: throw AssertionError("Thought we got a result, but it's not.")
}

// Misc

fun setSystemProxy(host: String, port: String, user: String, password: String) {
    System.setProperty("http.proxyHost", host);
    System.setProperty("http.proxyPort", port);

    Authenticator.setDefault(
            object : Authenticator() {
                public override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(user, password.toCharArray())
                }
            }
    )
}

// randomly shuffle an array
fun permutation(array: IntArray, random: Random = Random()): IntArray {
    val size = array.size
    for (i in (size - 1) downTo 1) {
        val chosenIndex = random.nextInt(i + 1)
        val chosen = array[chosenIndex]
        array[chosenIndex] = array[i]
        array[i] = chosen
    }
    return array
}

// return digits of number
fun digits(number: Int, len: Int): IntArray {
    val digits = IntArray(len)
    var temp: Int = number
    for (j in (len - 1) downTo 0) {
        digits[j] = temp % 10
        temp /= 10
    }

    return digits
}


fun formatKopeykaSum(number: Long): String {
    val roubles = number / 100
    val kop = number % 100

    val decimal = BigDecimal(String.format("%d.%02d", roubles, kop))

    val trimmed = kopeykaSumFormatter.format(decimal).trim()
    val res = if (trimmed.endsWith("00")) trimmed.substring(0, trimmed.length - 3) else trimmed
    return res
}

val kopeykaSumFormatter: DecimalFormat by lazy {
    (NumberFormat.getCurrencyInstance(Locale("ru")) as DecimalFormat).apply {
        // decimalFormatSymbols.currencySymbol = "" // <-- this is not enough, code below works
        val symbols = decimalFormatSymbols
        symbols.currencySymbol = ""
        decimalFormatSymbols = symbols
        minimumFractionDigits = 2
    }
}



class DoOnceLatch() {
    private var done: Boolean = false

    fun doOnce(block: () -> Unit) {
        if (!done) {
            block.invoke()
            done = true
        }
    }
}

class SkipFirstLatch() {
    private var first: Boolean = true

    fun skipFirst(block: () -> Unit) {
        if (first) {
            first = false
        } else {
            block.invoke()
        }
    }
}
