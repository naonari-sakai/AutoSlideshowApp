package com.example.naonari.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQIEST_CODE = 100

    private var firsttime = true

    private var playstop = false

    private var cursordata: Cursor? = null

    private var mTimer: Timer? = null

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //現在のバージョンが6以上か判定する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                //ユーザーに許可を求める
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQIEST_CODE
                )
            }
        }


        nextButton.setOnClickListener {
            //自動再生のオンオフ判定
            if (playstop == false) {
                if (firsttime == true) {
                    //初期画像のセット
                    firsttime = false
                    imageViewset(cursordata!!)
                } else {
                    //次の画像へ移動
                    imageView.setImageDrawable(null)
                    imageView.setImageURI(null)
                    next(cursordata!!)
                }
            }
        }

        returnButton.setOnClickListener {
            //自動再生のオンオフ判定
            if (playstop == false) {
                if (firsttime == true) {
                    //初期画像のセット
                    firsttime = false
                    imageViewset(cursordata!!)
                } else {
                    //次の画像へ移動
                    imageView.setImageDrawable(null)
                    imageView.setImageURI(null)
                    returnAction(cursordata!!)
                }
            }
        }

        stopplay.setOnClickListener {
            if (playstop == false) {
                if (firsttime == true) {
                    //初期画像がセットされていない場合、初期画像をセットする
                    firsttime = false
                    imageViewset(cursordata!!)
                }
                //タイマースタート
                playstop = true
                stopplay.text = "一時停止"
                Timerstart()
            } else {
                if (firsttime == true) {
                    //初期画像がセットされていない場合、初期画像をセットする
                    firsttime = false
                    imageViewset(cursordata!!)
                }
                //タイマーストップ
                playstop = false
                stopplay.text = "自動再生"
                if (mTimer != null) {
                    mTimer!!.cancel()
                    mTimer = null
                }

            }


        }


    }

    //アクセス許可の結果を受け取る
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
                    .setMessage("このアプリを使用する為には、アルバムへのアクセス許可をする必要があります。")
                    .setPositiveButton("OK", { dialog, which ->
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSIONS_REQIEST_CODE
                        )
                    })
                    .setNegativeButton("No", { dialog, which ->
                        //許可されない場合、アプリを終了する
                        finish()
                    })
                    .show()
            } else {

            }
        }
    }

    //初期画像のセット
    private fun imageViewset(cursordata: Cursor) {
        val fieldIndex = cursordata!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursordata!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    }

    //次の画像へ移動
    private fun next(cursordata: Cursor) {
        if (cursordata!!.moveToNext() != true) {
            cursordata!!.moveToFirst()
        }
        val fieldIndex = cursordata!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursordata!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)

    }

    //onStopでcursorを閉じているため、onResumeでカーソルを開く
    override fun onResume() {
        fun getContentInfo(): Cursor {
            val resolver = contentResolver
            val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
            )
            cursor!!.moveToFirst()
            return cursor
        }
        cursordata = getContentInfo()
        super.onResume()
    }

    //前の画像へ戻る
    private fun returnAction(cursordata: Cursor) {

        if (cursordata!!.moveToPrevious() != true) {
            cursordata!!.moveToLast()
        }
        val fieldIndex = cursordata!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursordata!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)

    }


    //カーソルをonStopのタイミングで閉じる
    override fun onStop() {
        imageView.setImageURI(null)
        imageView.setImageDrawable(null)
        Log.d("test", "onStop")
        if (playstop == true) {
            playstop = false
            stopplay.text = "自動再生"
            if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
            }
            if (cursordata != null) {
                cursordata!!.close()
            }
            firsttime = true
            super.onStop()
        }
    }

    //2秒ごとに処理をするタイマー
    private fun Timerstart() {
        mTimer = Timer()

        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post {
                    imageView.setImageDrawable(null)
                    imageView.setImageURI(null)
                    next(cursordata!!)
                }
            }
        }, 2000, 2000)
    }
}












