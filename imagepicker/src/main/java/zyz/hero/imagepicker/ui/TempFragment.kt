package zyz.hero.imagepicker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


class TempFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        try {
            if (requestCode == REQUEST_CODE) {
                onPermissionResult?.invoke(grantResults.all { it == 0 })
            }
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_CODE) {
                onResult?.invoke(resultCode, data)
            }
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    var onResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
    var onPermissionResult: ((havePermission: Boolean) -> Unit)? = null
    var mFragmentManager: FragmentManager? = null

    companion object {
        fun startActivityForResult(
            fragmentManager: FragmentManager?,
            destination: Class<out AppCompatActivity>? = ImagePickerActivity::class.java,
            params: Bundle = Bundle(),
            onResult: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> }
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
            onPermissionResult: (havePermission: Boolean) -> Unit
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

        private const val TAG = "TempFragment"
        private const val REQUEST_CODE = 502
    }
}