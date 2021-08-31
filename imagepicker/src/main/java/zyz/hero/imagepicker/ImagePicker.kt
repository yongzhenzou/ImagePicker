package zyz.hero.imagepicker

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePicker private constructor() {
    private var maxCount: Int = 9
    private var showCamara = true
    private var maxImageCount: Int = -1
    private var maxVideoCount: Int = -1
    private var mediaType: MediaType = MediaType.IMAGE_AND_VIDEO

    /**
     * @param lifecycleOwner 传入fragment或activity
     * @param destination 配置的目标activity，可以传入null进行默认跳转
     * @param result 选择资源后的回调
     */
    fun pick(
        lifecycleOwner: LifecycleOwner,
        destination: Class<out AppCompatActivity>?,
        result: (resourceList: ArrayList<String>?) -> Unit
    ) {
        if (maxCount <= 0) {
            return kotlin.run {
                Log.e(TAG, "maxCount必须大于0")
            }
        }
        if (mediaType == MediaType.IMAGE_AND_VIDEO && ((maxImageCount > maxCount) or (maxVideoCount > maxCount)) && (maxImageCount > -1 && maxVideoCount > -1)) {
            return kotlin.run {
                Log.e(TAG, "混合选择时，maxImageCount和maxVideoCount只能设置一个，且必须小于等于maxCount")
            }
        }
        var fragmentManager = when (lifecycleOwner) {
            is Fragment -> lifecycleOwner.childFragmentManager
            is FragmentActivity -> lifecycleOwner.supportFragmentManager
            else -> null
        }
        TempFragment.requestPermission(
            fragmentManager,
            *(if (showCamara) (Permission.PERMISSION_CAMERA) else Permission.PERMISSION_READ_WRITE)
        ) {
            if (it) {
                TempFragment.startActivityForResult(
                    fragmentManager,
                    destination,
                    bundleOf(
                        "config" to PickConfig(
                            maxCount,
                            mediaType,
                            showCamara,
                            maxImageCount,
                            maxVideoCount,
                        )
                    )
                ) { code, data ->
                    if (code == Activity.RESULT_OK) {
                        result(data?.getStringArrayListExtra("result"))
                    }
                }
            }
        }

    }

    class Builder() {
        private var maxCount: Int = 9
        private var showCamara = true
        private var maxVideoCount: Int = -1
        private var maxImageCount: Int = -1
        private var mediaType: MediaType = MediaType.IMAGE_AND_VIDEO

        /**
         * 选取图片的最大数量
         */
        fun maxCount(count: Int): Builder {
            return this.also {
                it.maxCount = count
            }
        }

        /**
         *是否显示拍照
         */
        fun showCamara(showCamara: Boolean): Builder {
            return this.also {
                it.showCamara = showCamara
            }
        }

        /**
         *文件选择类型
         * @param mediaType MediaType.IMAGE,MediaType.Video,MediaType.IMAGE_AND_VIDEO
         * @see MediaType
         */
        fun mediaType(mediaType: MediaType): Builder {
            return this.also {
                it.mediaType = mediaType
            }
        }

        /**
         *同时选择视频和图片时视频最大可选取数量和maxVideoCount互斥
         */
        fun maxImageCount(maxImageCount: Int): Builder {
            return this.also {
                it.maxImageCount = maxImageCount
            }
        }

        /**
         *同时选择视频和图片时视频最大可选取数量和maxImageCount互斥
         */
        fun maxVideoCount(maxVideoCount: Int): Builder {
            return this.also {
                it.maxVideoCount = maxVideoCount
            }
        }

        fun build(): ImagePicker {
            return ImagePicker().also {
                it.maxCount = maxCount
                it.showCamara = showCamara
                it.maxVideoCount = maxVideoCount
                it.maxImageCount = maxImageCount
                it.mediaType = mediaType
            }
        }
    }

    companion object {
        private const val TAG = "ImagePicker"
        fun builder(): Builder {
            return Builder()
        }
    }
}