package zyz.hero.imagepicker.base

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zyz.hero.imagepicker.*
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.ui.ImageAdapter
import zyz.hero.imagepicker.utils.MD5Utils
import java.io.File
import java.io.FileOutputStream

/**
 * @author yongzhen_zou@163.com
 * @date 2021/9/6 8:59 下午
 */
abstract class BaseImagePickerFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private val pickConfig: PickConfig by lazy {
        arguments?.getSerializable("config") as PickConfig
    }
    var mediaType: MediaType? = null  //1：视频和图片、2：图片、3：视频
    var mediaList = mutableListOf<ImageBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        require(pickConfig != null) {
            "pickConfig can not be null"
        }
        mediaType = pickConfig?.selectType
        return inflater.inflate(R.layout.fragment_imagepicker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler) as RecyclerView
        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        (recycler.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recycler.adapter = ImageAdapter(requireContext(), pickConfig!!)
        initData()
    }

    private fun refreshData() {
        if (!mediaList.isNullOrEmpty()) {
                if (pickConfig.showCamara){
                    mediaList.add(0, ImageBean(isCamera = true))
                }
            (recycler.adapter as ImageAdapter).refreshItems(mediaList)
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            showLoading()
            mediaList.clear()
            when (mediaType) {
               is MediaType.ImageAndVideo -> {
                    fillImageData()
                    fillVideoData()
                    mediaList?.sortByDescending { it.date }
                }
                is MediaType.Image -> {
                    fillImageData()
                }
                is MediaType.Video -> {
                    fillVideoData()
                }
            }
            hideLoading()
            refreshData()
        }
    }

    suspend fun fillImageData() = withContext(Dispatchers.IO) {
        val imageCursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} desc"
        )

        imageCursor?.use {
            while (it.moveToNext()) {
                mediaList?.add(
                    ImageBean(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                        ),
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                        MediaType.Image,
                        memiType = it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Images.ImageColumns.MIME_TYPE
                            )
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED))
                    )
                )
            }
        }
    }

    private suspend fun fillVideoData() = withContext(Dispatchers.IO) {
        val videoCursor = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.VideoColumns.DATE_ADDED} desc"
        )
        Log.e("lajlfja",videoCursor.toString())
        videoCursor?.use {
            while (it.moveToNext()) {
                mediaList?.add(
                    ImageBean(
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                        ),
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        MediaType.Video,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)),
                        memiType = it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Video.VideoColumns.MIME_TYPE
                            )
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_ADDED))
                    )
                )
                Log.e("lajlfja",mediaList.toString())
            }
        }

    }

    fun complete() = (recycler.adapter as ImageAdapter).selectedData

    private fun getRightFile(fileName: String, extension: String?): File {
        var file =
            File(ImagePicker.getTempDir(requireContext()) + MD5Utils.stringToMD5(fileName) + ".$extension")
        while (file.exists()) {
            file =
                File(ImagePicker.getTempDir(requireContext()) + MD5Utils.stringToMD5(fileName + System.nanoTime()) + ".$extension")
        }
        return file.apply {
            createNewFile()
        }
    }

    fun init(pickConfig: PickConfig): BaseImagePickerFragment {
        return this.apply {
            arguments = Bundle().apply {
                putSerializable("config", pickConfig)
            }
        }
    }
    abstract fun hideLoading()

    abstract fun showLoading()
}