package com.yogesh.videoplayer.view.permission

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yogesh.videoplayer.R
import com.yogesh.videoplayer.databinding.ActivityAllowPermissionBinding
import com.yogesh.videoplayer.view.MainActivity

class AllowPermissionActivity : AppCompatActivity() {
    val STORAGE = 11
    private var intent: Intent? = null
    lateinit var activityAllowPermissionBinding: ActivityAllowPermissionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAllowPermissionBinding = ActivityAllowPermissionBinding.inflate(layoutInflater)
        setContentView(activityAllowPermissionBinding.root)

        clickEvents()
    }

    private fun clickEvents() {
        activityAllowPermissionBinding.btn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestRuntimePermissionFunc("manageStorage")
            } else {
                requestRuntimePermissionFunc("storage")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Environment.isExternalStorageManager()) {
                intent = Intent(this@AllowPermissionActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                startAnim()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this@AllowPermissionActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                intent = Intent(this@AllowPermissionActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                startAnim()
            }
        }
    }

    private fun startAnim() {
        activityAllowPermissionBinding.bg.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.leaf))
        activityAllowPermissionBinding.bg.start()
        activityAllowPermissionBinding.bg.setOnPreparedListener(OnPreparedListener { mp ->
            mp.isLooping = true
        })
    }

    private fun requestRuntimePermissionFunc(permissionName: String) {
        if (permissionName == "manageStorage") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    intent = Intent(this@AllowPermissionActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                }
            }
        } else if (permissionName == "storage") {
            if (ContextCompat.checkSelfPermission(
                    this@AllowPermissionActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                intent = Intent(this@AllowPermissionActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@AllowPermissionActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                val builder = AlertDialog.Builder(this@AllowPermissionActivity)
                builder.setMessage("Can't proceed without necessary permissions")
                    .setTitle("storage required")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Allow"
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@AllowPermissionActivity,
                            arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE
                        )
                    }
                    .setNegativeButton(
                        "Deny"
                    ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this@AllowPermissionActivity,
                    arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intent = Intent(this@AllowPermissionActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@AllowPermissionActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                val builder = AlertDialog.Builder(this@AllowPermissionActivity)
                builder.setMessage("Can't proceed without necessary permissions")
                    .setTitle("storage required")
                    .setCancelable(false)
                    .setPositiveButton("Allow") { dialog: DialogInterface, which: Int ->
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri =
                            Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        "Deny"
                    ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                    .show()
            } else {
                requestRuntimePermissionFunc("storage")
            }
        }
    }

}