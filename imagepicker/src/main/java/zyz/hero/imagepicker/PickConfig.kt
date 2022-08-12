package zyz.hero.imagepicker

import zyz.hero.imagepicker.sealeds.SelectType
import java.io.Serializable

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/30 10:47 上午
 */
data class PickConfig(
    var selectType: SelectType = SelectType.Image,
    var showCamara: Boolean = true,
    var maxImageCount: Int = 9,
    var maxVideoCount: Int = 9,
    var imageLoaderId: Int = -1,
) : Serializable