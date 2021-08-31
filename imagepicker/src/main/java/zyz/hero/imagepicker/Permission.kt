package zyz.hero.imagepicker

import android.Manifest

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 9:12 下午
 */
object  Permission {
     val PERMISSION_CAMERA = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
     val PERMISSION_READ_WRITE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}