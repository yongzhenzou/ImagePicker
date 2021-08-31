package zyz.hero.imagepicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 12:06 上午
 */
class ImagePickerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagepicker)
        var pickConfig = intent.getSerializableExtra("config") as? PickConfig
        pickConfig?.let {
            supportFragmentManager.beginTransaction().add(
                R.id.container,
                ImagePickerFragment.newInstance(it),
                null
            ).commitNow()
        }
    }
}