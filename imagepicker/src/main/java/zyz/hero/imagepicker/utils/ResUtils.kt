package zyz.hero.imagepicker.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.TYPE_VIDEO
import zyz.hero.imagepicker.sealeds.MediaType

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/8 7:46 下午
 */
object ResUtils {
    fun getImageData(context: Context): MutableList<ImageBean> {
        var dataList = mutableListOf<ImageBean>()
        val imageCursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} desc"
        )

        imageCursor?.use {
            while (it.moveToNext()) {
                dataList?.add(
                    ImageBean(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                        ),
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                        TYPE_IMG,
                        memiType = it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Images.ImageColumns.MIME_TYPE
                            )
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED))
                    )
                )
            }
        }
        return dataList
    }

    fun getVideoData(context: Context): MutableList<ImageBean> {
        var dataList = mutableListOf<ImageBean>()
        val videoCursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.VideoColumns.DATE_ADDED} desc"
        )
        videoCursor?.use {
            while (it.moveToNext()) {
                dataList.add(
                    ImageBean(
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                        ),
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        TYPE_VIDEO,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)),
                        memiType = it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Video.VideoColumns.MIME_TYPE
                            )
                        ),
                        date = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_ADDED))
                    )
                )
            }
        }
        return dataList
    }
}