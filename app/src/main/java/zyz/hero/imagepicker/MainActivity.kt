package zyz.hero.imagepicker

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zyz.hero.imagepicker.ext.pickResource
import zyz.hero.imagepicker.sealeds.SelectType
import zyz.hero.imagepicker.ui.dialog.SimpleLoadingDialog

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 5:44 下午
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var loadingDialog = SimpleLoadingDialog()
        findViewById<Button>(R.id.select).setOnClickListener { view ->
            pickResource {
                setSelectType(SelectType.Image)
                setMaxImageCount(6)
                setMaxVideoCount(9)
            }.asFile(showLoading = {
                loadingDialog.show(supportFragmentManager,null)
            }, hideLoading = {
                loadingDialog.dismiss()
            }){

            }.start(this)
        }
    }
}