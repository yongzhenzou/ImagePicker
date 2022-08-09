package zyz.hero.imagepicker.sealeds

import java.io.Serializable

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/30 7:52 下午
 */
sealed class SelectType:Serializable {
    object Image : SelectType()
    object Video : SelectType()
    object All : SelectType()
}