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
import kotlinx.coroutines.*
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.R
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.ui.ImageAdapter
import zyz.hero.imagepicker.utils.ResUtils
import zyz.hero.imagepicker.utils.TempFragment
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

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
                        items.add(1, ImageBean(it.uri, it.name, TYPE_IMG))
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
                withContext(Dispatchers.IO) {
                    when (mediaType) {
                        is MediaType.ImageAndVideo -> {
                            var images = async{ ResUtils.getImageData(requireContext()) }
                            var videos = async { ResUtils.getVideoData(requireContext()) }
                            mediaList.apply {
                                addAll(images.await())
                                addAll(videos.await())
                            }
                        }
                        is MediaType.Image -> {
                            var images = async{ ResUtils.getImageData(requireContext()) }
                            mediaList.apply {
                                addAll(images.await())
                            }
                        }
                        is MediaType.Video -> {
                            var videos = async { ResUtils.getVideoData(requireContext()) }
                            mediaList.apply {
                                addAll(videos.await())
                            }
                        }
                    }
                    mediaList?.sortByDescending { it.date }
                }
                hideLoading()
                refreshData()
        }
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