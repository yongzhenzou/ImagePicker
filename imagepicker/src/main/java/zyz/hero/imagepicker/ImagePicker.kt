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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import zyz.hero.imagepicker.imageLoader.ImageLoader
import zyz.hero.imagepicker.sealeds.SelectType
import zyz.hero.imagepicker.ui.ImagePickerActivity
import zyz.hero.imagepicker.utils.FileUtils
import zyz.hero.imagepicker.utils.HelperFragment
import java.io.File

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePicker private constructor() {
    private val id by lazy {
        generateId()
    }

    /**
     * 图片加载器
     */
    private var imageLoader: ImageLoader? = null
        set(value) {
            imageLoaders[id] = value
            field = value
        }

    /**
     *是否显示拍照
     */
    private var showCamara = true

    /**
     *同时选择视频和图片时视频最大可选取数量和maxVideoCount互斥
     */
    private var maxImageCount: Int = 9

    /**
     *同时选择视频和图片时视频最大可选取数量和maxImageCount互斥
     */
    private var maxVideoCount: Int = 9

    /**
     *文件选择类型
     * @see SelectType
     */
    private var selectType: SelectType = SelectType.Image
    private var showLoading: (() -> Unit)? = null
    private var hideLoading: (() -> Unit)? = null
    private var uriResult: ((resourceList: ArrayList<Uri>) -> Unit)? = null
    private var fileResult: ((resourceList: ArrayList<File>) -> Unit)? = null
    private var pathResult: ((resourceList: ArrayList<String>) -> Unit)? = null

    fun asUri(uriResult: (resourceList: ArrayList<Uri>) -> Unit) = apply {
        this.uriResult = uriResult
    }

    fun asFile(
        showLoading: () -> Unit = {},
        hideLoading: () -> Unit = {},
        fileResult: (resourceList: ArrayList<File>) -> Unit,
    ) = apply {
        this.showLoading = showLoading
        this.hideLoading = hideLoading
        this.fileResult = fileResult
    }

    fun asPath(
        showLoading: () -> Unit = {},
        hideLoading: () -> Unit = {},
        pathResult: (resourceList: ArrayList<String>) -> Unit,
    ) = apply {
        this.showLoading = showLoading
        this.hideLoading = hideLoading
        this.pathResult = pathResult
    }

    fun start(
        fragmentActivity: FragmentActivity,
        target: Class<out AppCompatActivity> = ImagePickerActivity::class.java,
    ) {
        realStart(fragmentActivity, target)
    }

    fun start(
        fragment: Fragment,
        target: Class<out AppCompatActivity> = ImagePickerActivity::class.java,
    ) {
        realStart(fragment.requireActivity(), target)
    }

    private fun realStart(
        activity: FragmentActivity,
        target: Class<out AppCompatActivity> = ImagePickerActivity::class.java,
    ) {
        if (checkParams()) {
            HelperFragment.requestPermission(activity.supportFragmentManager,
                permissions = if (showCamara) Permission.PERMISSION_CAMERA else Permission.PERMISSION_READ_WRITE) {
                if (it) {//这里再申请一遍权限，防止ACCESS_MEDIA_LOCATION错误
                    HelperFragment.requestPermission(activity.supportFragmentManager,
                        permissions = if (showCamara) Permission.PERMISSION_CAMERA else Permission.PERMISSION_READ_WRITE) {
                        HelperFragment.startActivityForResult(
                            activity.supportFragmentManager,
                            target,
                            Bundle().apply {
                                putSerializable("config", PickConfig(
                                    selectType,
                                    showCamara,
                                    maxImageCount,
                                    maxVideoCount,
                                    imageLoaderId = if (imageLoader == null) -1 else id
                                ))
                            }

                        ) { code, data ->
                            if (code == Activity.RESULT_OK) {
                                (data?.getSerializableExtra("result") as? ArrayList<ResBean>)?.let { dataList ->
                                    uriResult?.invoke(arrayListOf<Uri>().apply {
                                        dataList.mapTo(this) { it.uri!! }
                                    })
                                    fileResult?.let {
                                        activity.lifecycleScope.launch {
                                            showLoading?.invoke()
                                            var uriToFile = FileUtils.uriToFile(activity, dataList)
                                            it.invoke(uriToFile)
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
                                if (imageLoader != null) {
                                    imageLoaders.remove(id)
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun checkParams(): Boolean {
        if ((maxImageCount < 0) or (maxVideoCount < 0)) {
            return kotlin.run {
                log("'maxImageCount' or 'maxVideoCount' can not be smaller than 0")
                false
            }
        }
        when (selectType) {
            is SelectType.Image -> {
                if (maxImageCount <= 0) {
                    return kotlin.run {
                        log("maxImageCount must be greater than 0 when selecting pictures")
                        false
                    }
                }
            }
            is SelectType.Video -> {
                if (maxVideoCount <= 0) {
                    return kotlin.run {
                        log("maxVideoCount must be greater than 0 when selecting videos")
                        false
                    }
                }
            }
            is SelectType.All -> {
                if (maxImageCount <= 0 && maxVideoCount <= 0) {
                    return kotlin.run {
                        log("When selecting pictures and videos, at least one of maxImageCount and maxVideoCount must be greater than 0")
                        false
                    }
                }
            }
        }
        return true
    }

    class Builder {
        /**
         *是否显示拍照
         */
        private var showCamara = true

        /**
         *选取图片的最大数量
         */
        private var maxImageCount: Int = 9

        /**
         *选取视频的最大数量
         */
        private var maxVideoCount: Int = 9

        /**
         * 图片加载方式
         */
        private var imageLoader: ImageLoader? = null

        /**
         *文件选择类型
         * @see SelectType
         */
        private var selectType: SelectType = SelectType.All

        fun setShowCamara(showCamara: Boolean) = apply {
            this.showCamara = showCamara
        }

        fun setSelectType(selectType: SelectType) = apply {
            this.selectType = selectType
        }

        fun setMaxImageCount(maxImageCount: Int) = apply {
            this.maxImageCount = maxImageCount
        }

        fun setMaxVideoCount(maxVideoCount: Int) = apply {
            this.maxVideoCount = maxVideoCount
        }

        fun setImageLoader(imageLoader: ImageLoader) {
            this.imageLoader = imageLoader
        }

        fun build() = ImagePicker().also {
            it.maxImageCount = maxImageCount
            it.maxVideoCount = maxVideoCount
            it.showCamara = showCamara
            it.selectType = selectType
            it.imageLoader = imageLoader
        }


    }

    companion object {
        const val TAG = "ImagePicker"
        fun log(content: String) {
            if (BuildConfig.DEBUG) {
                Log.e("$TAG: ", content)
            }

        }

        /**
         * 在业务逻辑完成（如图片上传）页面关闭的时候可调用此方法清理选取图片（asFile、asPath方式获取图片）造成的缓存
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

        fun getTempDir(context: Context) = "${context.cacheDir.absolutePath}/image_pick/"
        private var id = 0;
        private fun generateId(): Int {
            return ++id;
        }

        val imageLoaders = hashMapOf<Int, ImageLoader?>()
        var globalImageLoader: ImageLoader? = null
    }
}