package zyz.hero.imagepicker.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import zyz.hero.imagepicker.*
import zyz.hero.imagepicker.ext.visible
import zyz.hero.imagepicker.sealeds.SelectType

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 1:34 上午
 */
class ImageAdapter(var context: Context, var pickConfig: PickConfig, val takePhoto: () -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_CAMARA = 0
        const val TYPE_RESOURCE = 1
    }

    var items = arrayListOf<ResBean>()
    var selectedData = arrayListOf<ResBean>()
    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int): Int {
        return if (items[position].isCamera) TYPE_CAMARA else TYPE_RESOURCE
    }

    open fun concatItems(newItems: MutableList<ResBean>?) {
        if (newItems != null && newItems.size > 0) {
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    open fun refreshItems(newItems: MutableList<ResBean>?) {
        if (newItems != null) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CAMARA) CameraHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_image_picker_camera, parent, false)) else ImageHolder(
            LayoutInflater.from(context).inflate(R.layout.item_image_picker_image, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_CAMARA) {
            (holder as? CameraHolder)?.also {
                holder.itemView.setOnClickListener {
                    takePhoto?.invoke()
                }
            }
        } else {
            (holder as? ImageHolder)?.apply {
                var imageBean = items[position]
                select.visible = when (pickConfig.selectType) {
                    is SelectType.Image -> pickConfig.maxImageCount > 1
                    is SelectType.Video -> pickConfig.maxVideoCount > 1
                    else -> true
                }
                durationLayout.visible = imageBean.type == TYPE_VIDEO
                if (imageBean.type == TYPE_VIDEO) {
                    var minutes = imageBean.duration / 1000 / 60
                    var seconds = imageBean.duration / 1000 % 60
                    duration.text = "${minutes}:${if (seconds >= 10) seconds else "0$seconds"}"
                }
                loadRes(context, imageBean.uri!!, image)
                if (imageBean.select) {
                    select.text = (selectedData.indexOf(imageBean) + 1).toString()
                    select.setBackgroundResource(R.drawable.image_picker_shape_select)
                    image.alpha = 0.6f
                } else {
                    image.alpha = 0.9f
                    select.text = null
                    select.setBackgroundResource(R.drawable.image_picker_shape_unselect)
                }
                select.setOnClickListener {
                    if (imageBean.select) {
                        imageBean.select = false
                        selectedData.remove(imageBean)
                        notifyItemChanged(items.indexOf(imageBean))
                        selectedData.filter { it.select }.forEach {
                            notifyItemChanged(items.indexOf(it))
                        }
                    } else {
                        if (imageBean.type == TYPE_IMG) {
                            if (selectedData.filter { it.type == TYPE_IMG }.size < pickConfig.maxImageCount) {
                                handleSelect(this, imageBean)
                            } else {
                                ImagePicker.log("Select up to ${pickConfig.maxImageCount} pictures")
                            }
                        } else {
                            if (selectedData.filter { it.type == TYPE_VIDEO }.size < pickConfig.maxVideoCount) {
                                handleSelect(this, imageBean)
                            } else {
                                ImagePicker.log("Select up to ${pickConfig.maxVideoCount} videos")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadRes(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context).load(uri).dontAnimate().into(imageView)
    }

    private fun handleSelect(holder: ImageHolder, imageBean: ResBean) {
        imageBean.select = true
        selectedData.add(imageBean)
        holder.select.text = (selectedData.indexOf(imageBean) + 1).toString()
        holder.select.setBackgroundResource(R.drawable.image_picker_shape_select)
        holder.image.alpha = 0.6f
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.image)
        var durationLayout = itemView.findViewById<ConstraintLayout>(R.id.durationLayout)
        var duration = itemView.findViewById<TextView>(R.id.duration)
        var select = itemView.findViewById<TextView>(R.id.select)
    }

    class CameraHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}