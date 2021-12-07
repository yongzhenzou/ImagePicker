package zyz.hero.imagepicker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zyz.hero.imagepicker.ext.pickResource
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.sealeds.ResultType
import zyz.hero.imagepicker.ui.TempFragment

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 5:44 下午
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.select).setOnClickListener {view->
            TempFragment.requestPermission(
                supportFragmentManager,
                *Permission.PERMISSION_CAMERA
            ) {
                pickResource(this){
                    maxCount(8)
                    maxImageCount(6)
                    mediaType(MediaType.ImageAndVideo)
                }.onResult {
                    Log.e("pathList", it.toString())
                }
            }
        }
    }
}