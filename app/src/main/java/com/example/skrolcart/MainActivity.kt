package com.example.skrolcart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skrolcart.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var videoArrayList:ArrayList<VideoModel>

    private val adapterVideo by lazy {
        AdapterVideo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Videos"
        loadDataFromFirebase()
        initViews()
        onClick()
    }

    private fun initViews() {
        binding.apply {
            rvVideo.layoutManager=LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            rvVideo.adapter=adapterVideo
        }
    }

    private fun loadDataFromFirebase() {
        videoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Videos")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                videoArrayList.clear()
                for (ds in snapshot.children)
                {
                    val modelVideo = ds.getValue(VideoModel::class.java)
                    videoArrayList.add(modelVideo!!)
                }
                adapterVideo.submitList(videoArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun onClick() {
        binding.fbAddVideo.setOnClickListener {
            startActivity(Intent(this, AddVideoActivity::class.java))
        }
    }
}