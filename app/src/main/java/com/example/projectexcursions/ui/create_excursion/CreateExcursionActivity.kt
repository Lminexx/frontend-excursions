package com.example.projectexcursions.ui.create_excursion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import com.example.projectexcursions.ui.utilies.ProgressBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateExcursionActivity : AppCompatActivity() {

    private lateinit var adapter: PhotoAdapter
    private lateinit var binding: ActivityExcursionCreateBinding
    private lateinit var progressBar: ProgressBar
    private val viewModel: CreateExcursionViewModel by viewModels()

    private val REQUEST_CODE_PERMISSION = 1003

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
        progressBar = ProgressBar()
    }

    private fun initCallback() {
        binding.buttonCreateExcursion.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 5000)
            viewModel.clickCreateExcursion()
        }

        binding.excursionDescription.movementMethod = ScrollingMovementMethod()

        binding.buttonSelectImage.setOnClickListener {
            checkPermissionsAndProceed()
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
                val title = binding.excursionTitle.text.toString().trim()
                val description = binding.excursionDescription.text.toString().trim()
                if (viewModel.isExcursionCorrect(this, title, description)) {
                    viewModel.createExcursion(this@CreateExcursionActivity, title, description)
                    progressBar.show(this)
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

    private fun checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSION)
            } else {
                openImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
            } else {
                openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImages.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}