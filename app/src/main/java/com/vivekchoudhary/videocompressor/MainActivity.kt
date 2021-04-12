package com.vivekchoudhary.videocompressor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vivekchoudhary.ffmpegcompressor.VideoCompression
import com.vivekchoudhary.ffmpegcompressor.VideoUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var filemanagerstring: String? = null
    private var selectedFilePath: String? = null
    val REQUEST_TAKE_GALLERY_VIDEO = 101
    private val REQUEST_CODE_PERMISSIONS = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        selectVideoButton.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                    Intent.createChooser(intent, "Select Video"),
                    REQUEST_TAKE_GALLERY_VIDEO
            )
        }

        compressVideoButton.setOnClickListener {
            if (selectedFilePath != null) {
                startCompressing()
            } else {
                Toast.makeText(applicationContext, "Please select a video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
            ) {
                requestPermission()
            } else {
                requestPermission()
            }
        } else {
//            startCamera()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) {
//                startCamera()
                Toast.makeText(this,
                        "Permissions granted by the user.",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun startCompressing() {
        outputPath.text = "COMPRESSED FILE PATH:"
        VideoCompression(context = this@MainActivity).startCompressing(selectedFilePath, object : VideoCompression.CompressionListener {
                override fun compressionFinished(
                        status: Int,
                        isVideo: Boolean,
                        fileOutputPath: String?
                ) {
                    runOnUiThread {
                        val filesize = VideoUtils.getFileSizeInMb(fileOutputPath)
                        outputPath.text = "COMPRESSED FILE PATH: \n" + fileOutputPath + " (" + filesize + " MB)"}

                }

                override fun onFailure(message: String?) {
                    runOnUiThread { outputPath.text = message }

                }

                override fun onProgress(progress: Int) {
                    runOnUiThread { compressionProgress.text = "COMPRESSION PROGRESS: \n" + progress.toString() + "%" }

                }

            })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val selectedImageUri: Uri? = data?.data
                filemanagerstring = selectedImageUri?.getPath()
                // MEDIA GALLERY
                selectedFilePath = getPath(selectedImageUri)
                val filesize = VideoUtils.getFileSizeInMb(selectedFilePath)
                videoFilePath.text = "VIDEO FILE SELECTED: \n " + selectedFilePath + " (" + filesize + " MB)"
                Log.d(TAG, "onActivityResult: $selectedFilePath")
            }
        }
    }

    fun getPath(uri: Uri?): String? {
        val projection =
                arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }
}




