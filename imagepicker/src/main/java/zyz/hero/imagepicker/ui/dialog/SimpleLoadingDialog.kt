package zyz.hero.imagepicker.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import zyz.hero.imagepicker.R

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/19 12:34 下午
 */
class SimpleLoadingDialog: BaseDialogFragment() {
    override fun createView(
        context: Context?,
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): View {
        setCanceledBack(false)
        isCancelable = false
        setDimEnabled(false)
        return inflater.inflate(R.layout.dialog_loading,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}