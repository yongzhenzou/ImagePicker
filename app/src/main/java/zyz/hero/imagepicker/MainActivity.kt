package zyz.hero.imagepicker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 5:44 下午
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.select).setOnClickListener {
            TempFragment.requestPermission(
                supportFragmentManager,
                *Permission.PERMISSION_READ_WRITE
            ) {
                ImagePicker.builder()
                    .maxCount(8)
                    .maxImageCount(6)
                    .mediaType(MediaType.IMAGE_AND_VIDEO)
                    .build().pick(this, ImagePickerActivity::class.java) {
                    Log.e("pathList", it.toString())
                }
            }
        }
    }
}