package zyz.hero.imagepicker

import android.view.View


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