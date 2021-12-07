package zyz.hero.imagepicker

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.sealeds.ResultType
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

    var result: ((resourceList: ArrayList<Uri>) -> Unit)? = null
    fun onResult(result: (resourceList: ArrayList<Uri>) -> Unit) {
        this.result = result
    }

    fun maxCount(count: Int) {
        this.maxCount = count
    }

    fun showCamara(showCamara: Boolean) {
        this.showCamara = showCamara
    }

    fun mediaType(mediaType: MediaType) {
        this.mediaType = mediaType
    }

    fun maxImageCount(maxImageCount: Int) {
        this.maxImageCount = maxImageCount
    }

    fun maxVideoCount(maxVideoCount: Int) {
        this.maxVideoCount = maxVideoCount
    }
    fun getMaxCount(): Int {
        return maxCount
    }

    fun isShowCamara(): Boolean {
        return showCamara
    }

    fun getMediaType(): MediaType {
        return mediaType
    }

    fun getMaxImageCount(): Int {
        return maxImageCount
    }

    fun getMaxVideoCount(): Int {
        return maxVideoCount
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

        private fun deleteCache(context: Context) {
            var file = File(getTempDir(context))
            if (file.exists()) {
                file.deleteRecursively()
            }
        }

        fun getTempDir(context: Context) = "${context.getExternalFilesDir(null)}/image_pick/"
        fun newInstance() = ImagePicker()
    }
}