package zyz.hero.imagepicker

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 6:38 下午
 */
data class ImageBean(
    var uri: Uri? = null,
    var name: String? = null,
    var type: String? = null,
    var duration: Long = 0,//时长
    var memiType: String? = null,
    var date: Long? = null,
    var select: Boolean = false,
    var isCamera: Boolean = false,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeLong(duration)
        parcel.writeString(memiType)
        parcel.writeValue(date)
        parcel.writeByte(if (select) 1 else 0)
        parcel.writeByte(if (isCamera) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageBean> {
        override fun createFromParcel(parcel: Parcel): ImageBean {
            return ImageBean(parcel)
        }

        override fun newArray(size: Int): Array<ImageBean?> {
            return arrayOfNulls(size)
        }
    }
}

const val TYPE_IMG = "image"
const val TYPE_VIDEO = "video"
