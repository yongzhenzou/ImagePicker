package zyz.hero.imagepicker.ext

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.ImagePicker
import zyz.hero.imagepicker.Permission
import zyz.hero.imagepicker.ui.ImagePickerActivity
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.utils.TempFragment
import zyz.hero.imagepicker.sealeds.MediaType

inline fun pickResource(
    activity: AppCompatActivity,
    config: ImagePicker.() -> Unit = {},
): ImagePicker {
    return ImagePicker.newInstance().apply(config).apply {
        if (checkParams()) {
            TempFragment.requestPermission(activity.supportFragmentManager,permissions = Permission.PERMISSION_CAMERA){
                if (it){
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
                            (data?.getSerializableExtra("result") as? ArrayList<ImageBean>)?.let {dataList->
                                result?.invoke(arrayListOf<Uri>().apply{
                                    dataList.mapTo(this){it.uri!!}
                                })
                            }
                        }
                    }
                }
            }

        }
    }
}

//{
//lifecycleScope.launch {
//
//    var dir = File(ImagePicker.getTempDir(requireContext()))
//    if (!dir.exists()) {
//        dir.mkdirs()
//    }
//    showLoading()
//    flow {
//        var result = arrayListOf<String>()
//        var selectedData = (recycler.adapter as ImageAdapter).selectedData
//        selectedData.forEach { data ->
//            var inputStream = requireContext().contentResolver.openInputStream(data.uri)
//            inputStream?.let { inputStream ->
//                var rightFile = getRightFile(data.name,
//                    MimeTypeMap.getSingleton()
//                        .getExtensionFromMimeType(requireContext().contentResolver.getType(
//                            data.uri)))
//                inputStream.use { inputStream ->
//                    var outStream = java.io.FileOutputStream(rightFile)
//                    outStream?.use { outStream ->
//                        inputStream.copyTo(outStream)
//                        result.add(rightFile.absolutePath)
//                    }
//
//                }
//            }
//        }
//        emit(result)
//    }
//        .flowOn(Dispatchers.IO)
//        .onEach {
//            onComplete?.invoke(it)
//        }
//        .catch { e ->
//            onError?.invoke(e)
//        }.onCompletion {
//            hideLoading()
//        }.collect()
//}
//
//}

inline fun ImagePicker.checkParams(): Boolean {
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