package zyz.hero.imagepicker.ui

import android.os.Bundle
import android.widget.Toast
import zyz.hero.imagepicker.base.BaseImagePickerFragment

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePickerFragment : BaseImagePickerFragment() {

    override fun hideLoading() {
        Toast.makeText(requireContext(), "加载完成...", Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        Toast.makeText(requireContext(), "加载中...", Toast.LENGTH_SHORT).show()
    }

}