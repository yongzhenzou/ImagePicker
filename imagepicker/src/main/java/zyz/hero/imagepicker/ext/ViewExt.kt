package zyz.hero.imagepicker.ext

import android.view.View

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/7 11:42 上午
 */

var View.visible: Boolean
    set(visible) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }
    get() {
        return visibility == View.VISIBLE
    }
var View.inVisible: Boolean
    set(inVisible) {
        visibility = if (inVisible) View.INVISIBLE else View.VISIBLE
    }
    get() {
        return visibility == View.INVISIBLE
    }
