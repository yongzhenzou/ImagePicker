package zyz.hero.imagepicker

import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var a = File("/Users/zouyongzhen/AndroidStudioProjects/ImagePicker/imagepicker/src/main/res/drawable-xxhdpi/icon_video")
        println(a.exists())
        assertEquals(4, 2 + 2)
    }
}