package zyz.hero.imagepicker

import java.io.Serializable

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/30 10:47 上午
 */
data class PickConfig(
    var maxCount: Int = 9,
    var selectType:MediaType = MediaType.IMAGE_AND_VIDEO,
    var showCamara: Boolean = true,
    var maxImageCount:Int = -1,
    var maxVideoCount:Int = -1,
) : Serializable