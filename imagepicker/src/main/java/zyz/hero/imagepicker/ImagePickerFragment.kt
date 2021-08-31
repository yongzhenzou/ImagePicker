package zyz.hero.imagepicker

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePickerFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private  val pickConfig: PickConfig? by lazy {
        arguments?.getSerializable("config") as? PickConfig
    }
    var mediaType = 1 //1：视频和图片、2：图片、3：视频
    var mediaList: MutableList<ImageBean>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_imagepicker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler) as RecyclerView
        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler.adapter = ImageAdapter(requireContext(),pickConfig!!)
        initData()
        showData()
    }

    private fun showData() {
        if (!mediaList.isNullOrEmpty()) {
            (recycler.adapter as ImageAdapter).refreshItems(mediaList)
        }
    }

    private fun initData() {
        mediaList = mutableListOf()
        when (mediaType) {
            1 -> {
                fillImageData()
                fillVideoData()
                mediaList?.sortByDescending { it.date }
            }
            2 -> {
                fillImageData()
            }
            3 -> {
                fillVideoData()
            }
        }

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

    fun getSelectPaths(): ArrayList<Uri> {
        return ArrayList<Uri>().apply {
            addAll((recycler.adapter as ImageAdapter)?.items.map { it.uri })
        }
    }

    // TODO: 2021/8/31 这里返回具体地址需要把选中图片copy一份到应用私有目录然后返回私有目录中的地址
    fun complete(resourcePath: ArrayList<String>?) {
        requireActivity().apply {
            setResult(Activity.RESULT_OK, Intent().apply {
//                putStringArrayListExtra("result", resourcePath ?: getSelectPaths())
                finish()
            })
        }
    }

    companion object {
        fun newInstance(pickConfig: PickConfig): ImagePickerFragment {
            return ImagePickerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("config", pickConfig)
                }
            }
        }
    }
}