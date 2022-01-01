package zyz.hero.imagepicker.ui

import zyz.hero.imagepicker.base.BaseImagePickerFragment
import zyz.hero.imagepicker.ui.dialog.SimpleLoadingDialog

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePickerFragment : BaseImagePickerFragment() {
    val progressDialog :SimpleLoadingDialog by lazy {
        SimpleLoadingDialog()
    }

    override fun hideLoading() {
        progressDialog.dismiss()
    }

    override fun showLoading() {
        progressDialog.show(childFragmentManager,null)
    }

}