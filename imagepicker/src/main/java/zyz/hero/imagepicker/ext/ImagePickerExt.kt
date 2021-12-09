package zyz.hero.imagepicker.ext

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.ImagePicker
import zyz.hero.imagepicker.Permission
import zyz.hero.imagepicker.ui.ImagePickerActivity
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.utils.TempFragment
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

fun pickResource(
    activity: AppCompatActivity,
    config: ImagePicker.() -> Unit = {},
): ImagePicker {
    return ImagePicker.newInstance().apply(config).apply {
        if (checkParams()) {
            TempFragment.requestPermission(activity.supportFragmentManager,
                permissions = Permission.PERMISSION_CAMERA) {
                if (it) {
                    TempFragment.startActivityForResult(
                        activity.supportFragmentManager,
                        ImagePickerActivity::class.java,
                        Bundle().apply {
                            putSerializable("config", PickConfig(
                                getMaxCount(),
                                getMediaType(),
                                isShowCamara(),
                                getMaxImageCount(),
                                getMaxVideoCount(),
                            ))
                        }

                    ) { code, data ->
                        if (code == Activity.RESULT_OK) {
                            (data?.getSerializableExtra("result") as? ArrayList<ImageBean>)?.let { dataList ->
                                uriResult?.invoke(arrayListOf<Uri>().apply {
                                    dataList.mapTo(this) { it.uri!! }
                                })
                                fileResult?.let {
                                    activity.lifecycleScope.launch {
                                        it.invoke(FileUtils.uriToFile(activity, dataList))
                                    }
                                }
                                pathResult?.let {
                                    activity.lifecycleScope.launch {
                                        it.invoke(FileUtils.uriToFile(activity,
                                            dataList).mapTo(arrayListOf()) { it.absolutePath })
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

fun ImagePicker.checkParams(): Boolean {
    if (getMaxCount() <= 0) {
        return kotlin.run {
            Log.e(ImagePicker.TAG, "maxCount must be bigger than 0")
            false
        }
    }
    if (getMediaType() == MediaType.ImageAndVideo) {
        if ((getMaxImageCount() > -1 && getMaxVideoCount() > -1)) {
            return kotlin.run {
                Log.e(ImagePicker.TAG,
                    "Only one of 'maxImageCount' and 'maxVideoCount' can be set when mixing selection")

                false
            }
        }
        if (((getMaxImageCount() > getMaxCount()) or (getMaxVideoCount() > getMaxCount()))) {
            return kotlin.run {
                Log.e(ImagePicker.TAG,
                    "During mixed selection, only one 'maxImageCount' and 'maxVideoCount' can be set, and must be less than or equal to 'maxCount'")
                false
            }
        }
    }
    return true
}