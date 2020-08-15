package com.example.naonari.autoslideshowapp

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.provider.FontsContractCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQIEST_CODE = 100

    private val cursordata = getContentInfo()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQIEST_CODE
                )
            }
        }

        nextButton.setOnClickListener {
            next()
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQIEST_CODE -> if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                    .setCancelable(false)
                    .setTitle("ご注意")
                    .setMessage("このアプリを使用するには、外部ストレージへのアクセス許可をする必要があります。")
                    .setPositiveButton("OK", { dialog, which ->
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSIONS_REQIEST_CODE
                        )
                    })
                    .setNegativeButton("No", { dialog, which ->
                        finish()
                    })
                    .show()
            } else {

            }
        }
    }

    private fun getContentInfo(): Cursor? {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
        )
        return cursor
    }

    private fun next() {
        val fieldIndex = cursordata!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursordata!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
        cursordata!!.moveToNext()
    }


}








