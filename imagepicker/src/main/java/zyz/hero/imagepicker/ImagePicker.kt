package zyz.hero.imagepicker

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.sealeds.ResultType
import zyz.hero.imagepicker.ui.ImagePickerActivity
import zyz.hero.imagepicker.utils.FileUtils
import zyz.hero.imagepicker.utils.TempFragment
import java.io.File

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePicker private constructor() {
    /**
     * 选取图片的最大数量
     */
    private var maxCount: Int = 9

    /**
     *是否显示拍照
     */
    private var showCamara = true

    /**
     *同时选择视频和图片时视频最大可选取数量和maxVideoCount互斥
     */
    private var maxImageCount: Int = -1

    /**
     *同时选择视频和图片时视频最大可选取数量和maxImageCount互斥
     */
    private var maxVideoCount: Int = -1

    /**
     *文件选择类型
     * @see MediaType
     */
    private var mediaType: MediaType = MediaType.ImageAndVideo
    private var showLoading:(()->Unit)? = null
    private var hideLoading:(()->Unit)? = null

    fun showLoading(showLoading:()->Unit) = apply {
        this.showLoading = showLoading
    }
    fun hideLoading(hideLoading:()->Unit) = apply {
        this.hideLoading = hideLoading
    }

    private var uriResult: ((resourceList: ArrayList<Uri>) -> Unit)? = null
    private var fileResult: ((resourceList: ArrayList<File>) -> Unit)? = null
    private var pathResult: ((resourceList: ArrayList<String>) -> Unit)? = null

    fun asUri(uriResult: (resourceList: ArrayList<Uri>) -> Unit) = apply {
        this.uriResult = uriResult
    }

    fun asFile(fileResult: (resourceList: ArrayList<File>) -> Unit) = apply {
        this.fileResult = fileResult
    }

    fun asPath(pathResult: (resourceList: ArrayList<String>) -> Unit) = apply {
        this.pathResult = pathResult
    }

    fun start(activity: FragmentActivity) {
        realStart(activity)
    }

    fun start(fragment: Fragment) {
        realStart(fragment.requireActivity())
    }

    private fun realStart(activity: FragmentActivity) {
        if (checkParams()) {
            TempFragment.requestPermission(activity.supportFragmentManager,
                permissions = Permission.PERMISSION_CAMERA) {
                if (it) {
                    TempFragment.startActivityForResult(
                        activity.supportFragmentManager,
                        ImagePickerActivity::class.java,
                        Bundle().apply {
                            putSerializable("config", PickConfig(
                                maxCount,
                                mediaType,
                                showCamara,
                                maxImageCount,
                                maxVideoCount,
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
                                        showLoading?.invoke()
                                        it.invoke(FileUtils.uriToFile(activity, dataList))
                                        hideLoading?.invoke()
                                    }
                                }
                                pathResult?.let {
                                    activity.lifecycleScope.launch {
                                        showLoading?.invoke()
                                        it.invoke(FileUtils.uriToFile(activity,
                                            dataList).mapTo(arrayListOf()) { it.absolutePath })
                                        hideLoading?.invoke()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun checkParams(): Boolean {
        if (maxCount <= 0) {
            return kotlin.run {
                Log.e(ImagePicker.TAG, "maxCount must be bigger than 0")
                false
            }
        }
        if (mediaType == MediaType.ImageAndVideo) {
            if ((maxImageCount > -1 && maxVideoCount > -1)) {
                return kotlin.run {
                    Log.e(ImagePicker.TAG,
                        "Only one of 'maxImageCount' and 'maxVideoCount' can be set when mixing selection")

                    false
                }
            }
            if (((maxImageCount > maxCount) or (maxVideoCount > maxCount))) {
                return kotlin.run {
                    Log.e(ImagePicker.TAG,
                        "During mixed selection, only one 'maxImageCount' and 'maxVideoCount' can be set, and must be less than or equal to 'maxCount'")
                    false
                }
            }
        }
        return true
    }
    class Builder{
        /**
         * 选取图片的最大数量
         */
        private var maxCount: Int = 9

        /**
         *是否显示拍照
         */
        private var showCamara = true

        /**
         *同时选择视频和图片时视频最大可选取数量和maxVideoCount互斥
         */
        private var maxImageCount: Int = -1

        /**
         *同时选择视频和图片时视频最大可选取数量和maxImageCount互斥
         */
        private var maxVideoCount: Int = -1

        /**
         *文件选择类型
         * @see MediaType
         */
        private var mediaType: MediaType = MediaType.ImageAndVideo
        fun maxCount(count: Int) = apply {
            this.maxCount = count
        }

        fun showCamara(showCamara: Boolean) = apply {
            this.showCamara = showCamara
        }

        fun mediaType(mediaType: MediaType) = apply {
            this.mediaType = mediaType
        }

        fun maxImageCount(maxImageCount: Int) =apply {
            this.maxImageCount = maxImageCount
        }

        fun maxVideoCount(maxVideoCount: Int) = apply {
            this.maxVideoCount = maxVideoCount
        }
        fun build() = ImagePicker().also {
           it.maxCount = maxCount
            it.maxImageCount = maxImageCount
            it.maxVideoCount = maxVideoCount
            it.showCamara = showCamara
            it.mediaType = mediaType
        }


    }
    companion object {
        const val TAG = "ImagePicker"
        /**
         * 在业务逻辑完成（如图片上传）页面关闭的时候可调用此方法清理选取图片造成的缓存
         */
        fun clearCache(context: Context) = runBlocking {
            flow {
                emit(deleteCache(context))
            }
                .flowOn(Dispatchers.IO)
                .catch { it.printStackTrace() }
                .collect()
        }

        inline fun deleteCache(context: Context) {
            var file = File(getTempDir(context))
            if (file.exists()) {
                file.deleteRecursively()
            }
        }

        inline fun getTempDir(context: Context) = "${context.cacheDir.absolutePath}/image_pick/"

    }
}