package com.example.skrolcart

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skrolcart.databinding.ActivityAddVideoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddVideoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddVideoBinding.inflate(layoutInflater)
    }

    //actionbar
    private val VIDEO_PICK_CAMERA_CODE = 100
    private val VIDEO_PICK_GALLERY_CODE = 101
    private val CAMERA_REQUEST_CODE = 102

    lateinit var cameraPermission: Array<String>
    private lateinit var progressDialog: ProgressDialog
    private var videoUri: Uri? = null
    private var title:String=""

//    lateinit var actionBar: ActionBar

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //initActionBar
//        actionBar = supportActionBar!!
//        actionBar.title = "Add new Video"
//        actionBar.setDisplayHomeAsUpEnabled(true)
//        actionBar.setDefaultDisplayHomeAsUpEnabled(true)
        onClick()
        //initCameraPermissionArray
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("uploading Video")
        progressDialog.setCanceledOnTouchOutside(false)


    }

    private fun onClick() {
        binding.apply {
            btnUplod.setOnClickListener {
                title = binding.etTitle.text.toString().trim()
                if (TextUtils.isEmpty(title))
                {
                    Toast.makeText(this@AddVideoActivity, "Tittle is required", Toast.LENGTH_SHORT).show()
                }
                else if (videoUri == null)
                {
                    Toast.makeText(this@AddVideoActivity, "Pick the video firest", Toast.LENGTH_SHORT).show()
                }
                else{
                    uploadVideoFirebase()
                }

            }


            fbVideoPicker.setOnClickListener {
                Log.d("Tag","isVideoUploadedOrNot")
                videoPickDialog()
            }
        }

    }

    private fun uploadVideoFirebase() {
        progressDialog.show()
        val timestamp = ""+System.currentTimeMillis()
        val filePathAndName="Videos/video_$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapShot ->
                val uriTask = taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result
                if (uriTask.isSuccessful)
                {
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = "$timestamp"
                    hashMap["title"]="$title"
                    hashMap["timestamp"]="$timestamp"
                    hashMap["videoUri"]="$downloadUri"

                    val dbReference = FirebaseDatabase.getInstance().getReference("Videos")
                    dbReference.child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Video Uploaded", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun videoPickDialog() {
        val option = arrayOf("camera", "Gallery")
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("pick video From")
            .setItems(option) { dialogInteface, i ->
                if (i == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission()
                    } else {
                        videoPickFromCamera()
                    }
                } else {
                    videoPickFromGallery()
                }
            }.show()

    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return result1 && result2
    }


    private fun videoPickFromGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent, "Choose Video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun videoPickFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            CAMERA_REQUEST_CODE->
                if (grantResults.isNotEmpty())
                {
                    val cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED
                    val storageAccepted =grantResults[1]==PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted)
                    {

                    }
                    else{
                        Toast.makeText(this, "Permission Denied" , Toast.LENGTH_SHORT).show()
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode==VIDEO_PICK_CAMERA_CODE)
            {
                videoUri==data!!.data
                setVideoToVideoView()
            }
            else if (requestCode==VIDEO_PICK_GALLERY_CODE)
            {
                videoUri=data!!.data
                setVideoToVideoView()
            }
        }
        else{
            Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setVideoToVideoView() {
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.requestFocus()
        binding.videoView.setOnPreparedListener {
            binding.videoView.start()
        }
    }
}