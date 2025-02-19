package com.example.projectexcursions.ui.fullscreen

import FullScreenPhotoAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityFullScreenPhotoBinding

class FullScreenPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenPhotoBinding
    private lateinit var photoAdapter: FullScreenPhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoList = intent.getParcelableArrayListExtra<Uri>(EXTRA_PHOTOS) ?: emptyList()
        val startPosition = intent.getIntExtra(EXTRA_POSITION, 0)

        photoAdapter = FullScreenPhotoAdapter(photoList)
        binding.viewPager.adapter = photoAdapter
        binding.viewPager.setCurrentItem(startPosition, false)

        binding.buttonClose.setOnClickListener {
            finish()
        }
    }

    companion object {
        private const val EXTRA_PHOTOS = "extra_photos"
        private const val EXTRA_POSITION = "extra_position"

        fun createIntent(context: Context, photoList: List<Uri>, position: Int): Intent {
            return Intent(context, FullScreenPhotoActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_PHOTOS, ArrayList(photoList))
                putExtra(EXTRA_POSITION, position)
            }
        }
    }
}
