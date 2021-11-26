package zyz.hero.imagepicker.base

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import zyz.hero.imagepicker.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

/**
 * @author yongzhen_zou@163.com
 * @date 2021/9/6 8:59 下午
 */
abstract class BaseImagePickerFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private val pickConfig: PickConfig? by lazy {
        arguments?.getSerializable("config") as? PickConfig
    }
    var mediaType: MediaType? = null  //1：视频和图片、2：图片、3：视频
    var mediaList: MutableList<ImageBean>? = null
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
        recycler.adapter = ImageAdapter(requireContext(), pickConfig!!)
        (recycler.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        initData()
    }

    private fun showData() {
        if (!mediaList.isNullOrEmpty()) {
            (recycler.adapter as ImageAdapter).refreshItems(mediaList)
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            showLoading()
            mediaList = mutableListOf()
            when (mediaType) {
                MediaType.IMAGE_AND_VIDEO -> {
                    fillImageData()
                    fillVideoData()
                    mediaList?.sortByDescending { it.date }
                }
                MediaType.IMAGE -> {
                    fillImageData()
                }
                MediaType.VIDEO -> {
                    fillVideoData()
                }
            }
            hideLoading()
            showData()
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
                        MediaType.IMAGE,
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
        videoCursor?.use {
            while (it.moveToNext()) {
                mediaList?.add(
                    ImageBean(
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                        ),
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        MediaType.VIDEO,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)),
                        memiType = it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Video.VideoColumns.MIME_TYPE
                            )
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_ADDED))
                    )
                )
            }
        }

    }

    fun complete(
        onError: ((e: Throwable) -> Unit)? = null,
        onComplete: (filePaths: ArrayList<String>) -> Unit,
    ) {
        lifecycleScope.launch {
            var dir = File(ImagePicker.getTempDir(requireContext()))
            if (!dir.exists()) {
                dir.mkdirs()
            }
            showLoading()
            flow {
                var result = arrayListOf<String>()
                var selectedData = (recycler.adapter as ImageAdapter).selectedData
                selectedData.forEach { data ->
                    var inputStream = requireContext().contentResolver.openInputStream(data.uri)
                    inputStream?.let { inputStream ->
                        var rightFile = getRightFile(data.name,
                            MimeTypeMap.getSingleton()
                                .getExtensionFromMimeType(requireContext().contentResolver.getType(
                                    data.uri)))
                        inputStream.use { inputStream ->
                            var outStream = FileOutputStream(rightFile)
                            outStream?.use { outStream ->
                                inputStream.copyTo(outStream)
                                result.add(rightFile.absolutePath)
                            }

                        }
                    }
                }
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .onEach {
                    onComplete?.invoke(it)
                }
                .catch { e ->
                    onError?.invoke(e)
                }.onCompletion {
                    hideLoading()
                }.collect()
        }

    }

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

    override fun onDestroy() {
        super.onDestroy()
    }

    abstract fun hideLoading()

    abstract fun showLoading()
}