package zyz.hero.imagepicker.base

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.R
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.ui.ImageAdapter
import zyz.hero.imagepicker.utils.TempFragment

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
        recycler.adapter = ImageAdapter(requireContext(), pickConfig!!) {
            takePhoto()
        }
        initData()
    }

    private fun takePhoto() {
        TempFragment.takePhoto(childFragmentManager) { data ->
            if (pickConfig.showCamara) {
                data?.let {
                    (recycler.adapter as ImageAdapter).apply {
                        items.add(1, ImageBean(it.uri, it.name, MediaType.Image))
                        notifyItemInserted(1)
                    }
                }

            }
        }
    }

    private fun refreshData() {
        if (!mediaList.isNullOrEmpty()) {
            if (pickConfig.showCamara) {
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
                    withContext(Dispatchers.IO) {
                        mediaList.addAll(withContext(Dispatchers.Default) {
                            getImageData()
                        })
                        mediaList.addAll(withContext(Dispatchers.Default) {
                            getVideoData()
                        })
                    }
                    mediaList?.sortByDescending { it.date }
                }
                is MediaType.Image -> {
                    mediaList.addAll(withContext(Dispatchers.Default) {
                        getImageData()
                    })
                }
                is MediaType.Video -> {
                    mediaList.addAll(withContext(Dispatchers.Default) {
                        getVideoData()
                    })
                }
            }
            hideLoading()
            refreshData()
        }
    }

    private fun getImageData(): MutableList<ImageBean> {
        var dataList = mutableListOf<ImageBean>()
        val imageCursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} desc"
        )

        imageCursor?.use {
            while (it.moveToNext()) {
                dataList?.add(
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
        return dataList
    }

    private fun getVideoData(): MutableList<ImageBean> {
        var dataList = mutableListOf<ImageBean>()
        val videoCursor = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.VideoColumns.DATE_ADDED} desc"
        )
        videoCursor?.use {
            while (it.moveToNext()) {
                dataList.add(
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
            }
        }
        return dataList
    }

    fun complete() = (recycler.adapter as ImageAdapter).selectedData

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