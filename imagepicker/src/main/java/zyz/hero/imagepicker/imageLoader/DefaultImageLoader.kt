package zyz.hero.imagepicker.imageLoader

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * @author zouyongzhen
 * @date 2022/8/12 09:27
 */
class DefaultImageLoader:ImageLoader {
    override fun load(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context).load(uri).into(imageView)
    }
}