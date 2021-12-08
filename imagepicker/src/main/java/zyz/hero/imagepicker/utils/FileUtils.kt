package zyz.hero.imagepicker.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/7 5:52 下午
 */
class FileUtils {
    companion object{
        fun getFileUri(context:Context,filePath:String):Uri{
            var file = File(filePath)
            return  if (Build.VERSION.SDK_INT >= 24)
                FileProvider.getUriForFile(context, "zyz.hero.imagepicker.fileprovider", file)
            else
                Uri.fromFile(file)
        }
    }
}