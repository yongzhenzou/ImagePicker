package zyz.hero.imagepicker

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        println(Thread.currentThread().name)
        var threadLocal = ThreadLocal<String>()
        threadLocal.set("123")
        var threadLocal2 = ThreadLocal<String>()
        threadLocal2.set("456")
        println(threadLocal.get())
        println(threadLocal2.get())
    }
}