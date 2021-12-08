package zyz.hero.imagepicker.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import zyz.hero.imagepicker.ImageBean
import zyz.hero.imagepicker.sealeds.MediaType
import zyz.hero.imagepicker.ui.ImagePickerActivity
import java.io.File


class TempFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        try {
            if (requestCode == REQUEST_CODE) {
                onPermissionResult?.invoke(grantResults.all { it == 0 })
            }
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }finally {
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_CODE) {
                onResult?.invoke(resultCode, data)
                if (resultCode==Activity.RESULT_OK){
                    captureResult?.invoke(imageBean)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }finally {
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }
    }

    var onResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
    var captureResult: ((ImageBean?) -> Unit)? = null
    var onPermissionResult: ((havePermission: Boolean) -> Unit)? = null
    var mFragmentManager: FragmentManager? = null

    companion object {
        fun startActivityForResult(
            fragmentManager: FragmentManager?,
            destination: Class<out AppCompatActivity>? = ImagePickerActivity::class.java,
            params: Bundle = Bundle(),
            onResult: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> },
        ) {
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            var tempFragment = TempFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG).commitNow()
            tempFragment.onResult = onResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.startActivityForResult(
                Intent(
                    tempFragment.activity,
                    destination
                ).putExtras(params), REQUEST_CODE
            )
        }

        fun requestPermission(
            fragmentManager: FragmentManager?,
            vararg permissions: String,
            onPermissionResult: (havePermission: Boolean) -> Unit,
        ) {
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            var tempFragment = TempFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG).commitNow()
            tempFragment.onPermissionResult = onPermissionResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.requestPermissions(permissions, REQUEST_CODE)
        }

        fun takePhoto(
            fragmentManager: FragmentManager?,
            captureResult: (ImageBean?) -> Unit = { },
        ) {
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            var tempFragment = TempFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG).commitNow()
            tempFragment.captureResult = captureResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.takePhoto()
        }

        private const val TAG = "TempFragment"
        private const val REQUEST_CODE = 502
    }
    var imageBean:ImageBean? = null
    private fun takePhoto() {
        var fileDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path)
        if (!fileDir.exists()){
            fileDir.mkdir()
        }
        var fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        var mFilePath = fileDir.absolutePath +"/"+ fileName;
        //
        var values =  ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            values.put(MediaStore.Images.Media.RELATIVE_PATH,"DCIM/Pictures")
        }else{
            values.put(MediaStore.Images.Media.DATA, mFilePath);
        }
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/JPEG")
        var uri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)!!
        imageBean = ImageBean(uri,fileName,MediaType.Image)
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }, REQUEST_CODE)
    }
}