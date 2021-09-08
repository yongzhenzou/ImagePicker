package zyz.hero.imagepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
        var newInstance = ImagePickerFragment().init(pickConfig!!)
        pickConfig?.let {
            supportFragmentManager.beginTransaction().add(
                R.id.container,
                newInstance,
                null
            ).commitNow()
        }
        findViewById<Button>(R.id.complete).setOnClickListener {
            newInstance.complete {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putStringArrayListExtra("result", it)
                })
                finish()
            }
        }
    }
}