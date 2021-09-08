package zyz.hero.imagepicker.base

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import zyz.hero.imagepicker.*
import java.io.File
import java.io.FileOutputStream

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
        require(pickConfig!=null){
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
        Thread {
            runOnUiThread {
                showLoading()
            }
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
            runOnUiThread {
                hideLoading()
                showData()
            }
        }.start()

    }

    private fun fillImageData() {
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

    private fun fillVideoData() {
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
        onError: ((e: Exception) -> Unit)? = null,
        onComplete: (filePaths: ArrayList<String>) -> Unit,
    ) {
        Thread {
            try {
                var result = arrayListOf<String>()
                var dir = File(ImagePicker.getTempDir(requireContext()))
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                runOnUiThread {
                    showLoading()
                }
                (recycler.adapter as ImageAdapter).selectedData.forEach { data ->
                    requireContext().contentResolver.openInputStream(data.uri)?.let { inputStream ->
                        var rightFile = getRightFile(
                            data.name,
                            MimeTypeMap.getSingleton()
                                .getExtensionFromMimeType(
                                    requireContext().contentResolver.getType(
                                        data.uri
                                    )
                                )
                        )

                        inputStream.use { inputStream ->
                            var outStream = FileOutputStream(rightFile)
                            outStream?.use { outStream ->
                                inputStream.copyTo(outStream)
                                result.add(rightFile.absolutePath)
                            }

                        }
                    }
                }
                runOnUiThread {
                    onComplete.invoke(result)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    onError?.invoke(e)
                }
            } finally {
                runOnUiThread {
                    hideLoading()
                }
            }
        }.start()

    }

    private fun runOnUiThread(block: () -> Unit) {
        requireActivity().runOnUiThread {
            block()
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

    abstract fun hideLoading()

    abstract fun showLoading()
}