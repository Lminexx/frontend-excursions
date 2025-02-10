package com.example.projectexcursions.ui.create_excursion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateExcursionActivity : AppCompatActivity() {

    private lateinit var adapter: PhotoAdapter
    private lateinit var binding: ActivityExcursionCreateBinding
    private val viewModel: CreateExcursionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initCallback()
        subscribe()
    }

    private fun initData() {
        adapter = PhotoAdapter(this, emptyList())
        binding.recyclerViewSelectedImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewSelectedImages.setHasFixedSize(true)
        binding.recyclerViewSelectedImages.adapter = adapter
    }

    private fun initCallback() {
        binding.buttonCreateExcursion.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 1000)
            viewModel.clickCreateExcursion()
        }

        binding.excursionDescription.movementMethod = ScrollingMovementMethod()

        binding.buttonSelectImage.setOnClickListener {
            pickImagesFromGallery()
        }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeBack ->
            if (wannaComeBack) {
                finish()
            }
        }

        viewModel.createExcursion.observe(this) { wannaCreate ->
            if (wannaCreate) {
                Log.d("WantCreate", "WantCreate")
                val title = binding.excursionTitle.text.toString().trim()
                val description = binding.excursionDescription.text.toString().trim()
                if (viewModel.isExcursionCorrect(this, title, description)) {
                    viewModel.createExcursion(this@CreateExcursionActivity, title, description)
                }
            }
        }

        viewModel.selectedImages.observe(this) { selectedImages ->
            adapter.updatePhotos(selectedImages)
        }
    }

    private val pickImages = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val clipData = result.data?.clipData
            val imageUris = mutableListOf<Uri>()
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    imageUris.add(clipData.getItemAt(i).uri)
                }
            } else {
                result.data?.data?.let { imageUris.add(it) }
            }
            if (imageUris.isNotEmpty()) {
                viewModel.addSelectedImages(imageUris)
            }
        }
    }

    private fun pickImagesFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImages.launch(intent)
    }
}