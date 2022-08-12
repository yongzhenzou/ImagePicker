package zyz.hero.imagepicker

import android.Manifest
import android.os.Build

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 9:12 下午
 */
internal object Permission {
    val PERMISSION_CAMERA = when {
        Build.VERSION.SDK_INT < 29 -> arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else -> arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )
    }

    val PERMISSION_READ_WRITE = when {
        Build.VERSION.SDK_INT < 29 -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )
    }

}