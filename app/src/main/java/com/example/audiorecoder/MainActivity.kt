package com.example.audiorecoder

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var mRecorder: MediaRecorder? = null
    private lateinit var fileName: String
    private var isRecording = false
    private val STORAGE_PERMISSION_CODE = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imgBtn = findViewById<ImageButton>(R.id.btnPlayPause)

        imgBtn.setOnClickListener {
            if (!checkStoragePermissions())
                requestForStoragePermissions()
            else {
                var img = 0
                if (!isRecording) {
                    isRecording = true
                    startRecording()
                    img = R.drawable.baseline_pause_24
                } else {
                    isRecording = false
                    stopRecording("")
                    img = R.drawable.baseline_play_arrow_24
                }

                imgBtn.setImageResource(img)
            }
        }
    }

    private fun playAudio(){
        //play audio
    }

    private fun checkStoragePermissions(): Boolean {
            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val recording = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return recording == PackageManager.PERMISSION_GRANTED
            }else
                return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && recording == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri: Uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
            //Below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO
                ),
                STORAGE_PERMISSION_CODE
            )
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                if (Environment.isExternalStorageManager()) {
                    //Manage External Storage Permissions Granted
                    Log.d("soham", "onActivityResult: Manage External Storage Permissions Granted")
                    if (checkStoragePermissions()) {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), STORAGE_PERMISSION_CODE)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val recording = grantResults[2] == PackageManager.PERMISSION_GRANTED
                if (read && write && recording) {
                    Toast.makeText(this@MainActivity, "Storage Permissions Granted", Toast.LENGTH_SHORT).show()
                    if (checkStoragePermissions()) {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), STORAGE_PERMISSION_CODE)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startRecording() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            mRecorder = MediaRecorder()
        else
            mRecorder = MediaRecorder()

        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

        val root: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS)
        } else {
            Environment.getExternalStorageDirectory()
        }
        val file = File(root.path + "/s/Audios/")
        if (!file.exists()) {
            file.mkdirs()
        }
        val currentTime = System.currentTimeMillis().toString()
        fileName = root.absolutePath + "/s/Audios/" + ("$currentTime.mp4")
        mRecorder!!.setOutputFile(fileName)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder!!.setAudioEncodingBitRate(16000)
        mRecorder!!.setAudioChannels(1)
        mRecorder!!.setAudioSamplingRate(44100)
        try {
            mRecorder!!.prepare()
            mRecorder!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(close: String) {
        try {
            mRecorder!!.stop()
            mRecorder!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mRecorder = null
        if (TextUtils.isEmpty(close)) {
            //showing the play button
            Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show()
        } else {
            val fdelete = File(fileName)
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    println("file Deleted ")
                } else {
                    println("file not Deleted :")
                }
            }
        }
    }
}