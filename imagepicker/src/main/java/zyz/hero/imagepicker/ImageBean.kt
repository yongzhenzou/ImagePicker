package zyz.hero.imagepicker

import android.net.Uri
import zyz.hero.imagepicker.sealeds.MediaType
import java.io.Serializable

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 6:38 下午
 */
data class ImageBean(
    var uri: Uri? = null,
    var name:String? = null,
    var type: MediaType? = null,
    var duration:Long = 0,//时长
    var memiType:String? = null,
    var date:Long? = null,
    var select: Boolean = false,
    var isCamera:Boolean = false
):Serializable
