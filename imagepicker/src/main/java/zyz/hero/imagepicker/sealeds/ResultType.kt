package zyz.hero.imagepicker.sealeds

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/7 11:01 上午
 */
sealed class ResultType {
    object Uri : ResultType()
    object Path : ResultType()
    object File : ResultType()
    object Bitmap : ResultType()
}