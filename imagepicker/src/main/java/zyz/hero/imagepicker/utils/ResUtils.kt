package zyz.hero.imagepicker.utils

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import zyz.hero.imagepicker.ResBean
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.TYPE_VIDEO

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/8 7:46 下午
 */
object ResUtils {
    fun getImageData(context: Context): MutableList<ResBean> {
        var dataList = mutableListOf<ResBean>()
        val imageCursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} desc"
        )

        imageCursor?.use {
            while (it.moveToNext()) {
                var uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.setRequireOriginal(ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                    ))
                } else {
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                    )
                }
                dataList?.add(
                    ResBean(
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

    fun getVideoData(context: Context): MutableList<ResBean> {
        var dataList = mutableListOf<ResBean>()
        val videoCursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.VideoColumns.DATE_ADDED} desc"
        )
        videoCursor?.use {
            while (it.moveToNext()) {
                var uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.setRequireOriginal(ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                    ))
                } else {
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                    )
                }
                dataList.add(
                    ResBean(
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