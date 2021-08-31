package zyz.hero.imagepicker

import android.net.Uri

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 6:38 下午
 */
data class ImageBean(
    var uri: Uri,
    var name:String,
    var type:MediaType,
    var duration:Long = 0,//时长
    var memiType:String,
    var date:Long,
    var select: Boolean = false
)
