package zyz.hero.imagepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 1:34 上午
 */
class ImageAdapter(var context: Context, var pickConfig: PickConfig) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items = arrayListOf<ImageBean>()
    var selectedData = arrayListOf<ImageBean>()
    override fun getItemCount(): Int = items.size

    open fun concatItems(newItems: MutableList<ImageBean>?) {
        if (newItems != null && newItems.size > 0) {
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    open fun refreshItems(newItems: MutableList<ImageBean>?) {
        if (newItems != null) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ImageHolder)?.bindView(items[position], position)
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.image)
        var durationLayout = itemView.findViewById<ConstraintLayout>(R.id.durationLayout)
        var duration = itemView.findViewById<TextView>(R.id.duration)
        var select = itemView.findViewById<TextView>(R.id.select)

        fun bindView(imageBean: ImageBean, position: Int) {
            select.visible = pickConfig.maxCount > 1
            durationLayout.visible = imageBean.type == MediaType.VIDEO
            if (imageBean.type == MediaType.VIDEO) {
                var minutes = imageBean.duration / 1000 / 60
                var seconds = imageBean.duration / 1000 % 60
                duration.text = "${minutes}:${if (seconds >= 10) seconds else "0$seconds"}"
            }
            Glide.with(context).load(imageBean.uri).dontAnimate().into(image)
            if (imageBean.select) {
                select.text = (selectedData.indexOf(imageBean) + 1).toString()
                select.setBackgroundResource(R.drawable.shape_select)
                image.alpha = 0.6f
            } else {
                image.alpha = 0.9f
                select.text = null
                select.setBackgroundResource(R.drawable.shape_unselect)
            }
            select.setOnClickListener {
                if (imageBean.select) {
                    imageBean.select = false
                    selectedData.remove(imageBean)
                   notifyItemRangeChanged(0,items.size)

                } else {
                    if (selectedData.size >= pickConfig.maxCount) {
                        return@setOnClickListener Toast.makeText(
                            context,
                            "最多选取${pickConfig.maxCount}张图片或视频",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    when (pickConfig.selectType) {
                        MediaType.IMAGE_AND_VIDEO -> {
                            //混合选择是否设置了maxImageCount或者maxVideoCount，只能设置一个
                            if (imageBean.type == MediaType.IMAGE) {
                                if (pickConfig.maxImageCount != -1) {
                                    if (selectedData.filter { it.type == MediaType.IMAGE }.size < pickConfig.maxImageCount) {
                                        handleSelect(imageBean)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "最多选取${pickConfig.maxImageCount}张图片",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    handleSelect(imageBean)
                                }
                            } else {
                                if (pickConfig.maxVideoCount != -1) {
                                    if (selectedData.filter { it.type == MediaType.VIDEO }.size < pickConfig.maxVideoCount) {
                                        handleSelect(imageBean)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "最多选取${pickConfig.maxVideoCount}个视频",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    handleSelect(imageBean)
                                }
                            }
                        }
                        else -> {
                            //选取数量只受maxCount约束
                            handleSelect(imageBean)
                        }

                    }
                }
            }
        }

        private fun handleSelect(imageBean: ImageBean) {
            if (selectedData.size < pickConfig.maxCount) {
                imageBean.select = true
                selectedData.add(imageBean)
                select.text = (selectedData.indexOf(imageBean) + 1).toString()
                select.setBackgroundResource(R.drawable.shape_select)
                image.alpha = 0.6f
            } else {
                Toast.makeText(
                    context,
                    "最多选取${pickConfig.maxCount}张图片或视频",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}