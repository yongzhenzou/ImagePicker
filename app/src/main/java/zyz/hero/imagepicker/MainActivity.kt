package zyz.hero.imagepicker

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zyz.hero.imagepicker.ext.pickResource
import zyz.hero.imagepicker.sealeds.SelectType

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 5:44 下午
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.select).setOnClickListener { view ->
            pickResource {
                mediaType(SelectType.ImageAndVideo)
                maxImageCount(6)
                maxVideoCount(9)
            }.asFile {

            }.start(this)
        }
    }
}