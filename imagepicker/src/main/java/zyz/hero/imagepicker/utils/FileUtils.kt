package zyz.hero.imagepicker.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.ImagePicker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/7 5:52 下午
 */
class FileUtils {
    companion object {
        fun getFileUri(context: Context, filePath: String): Uri {
            var file = File(filePath)
            return if (Build.VERSION.SDK_INT >= 24)
                FileProvider.getUriForFile(context, "zyz.hero.imagepicker.fileprovider", file)
            else
                Uri.fromFile(file)
        }

        suspend fun uriToFile(
            activity: FragmentActivity,
            dataList: ArrayList<ImageBean>,
        ) = withContext(Dispatchers.IO) {
            dataList.map {
                async {
                    activity.contentResolver.openInputStream(it.uri!!).use { inputStream ->
                        var dir = File(ImagePicker.getTempDir(activity))
                        dir.mkdirs()
                        var file = File(ImagePicker.getTempDir(activity) + it.name)
                        if (file.exists()) {
                            file.delete()
                        }
                        FileOutputStream(file).use { outStream ->
                            inputStream?.copyTo(outStream)
                        }
                        file
                    }
                }
            }.mapTo(arrayListOf()) {
                it.await()
            }
        }
    }
}