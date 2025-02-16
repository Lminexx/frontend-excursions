package com.example.projectexcursions.ui.registration

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.example.projectexcursions.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegActivity : AppCompatActivity() {

    private lateinit var adapter: PhotoAdapter
    private lateinit var binding: ActivityRegBinding
    private val viewModel: RegViewModel by viewModels()

    private val REQUEST_CODE_PERMISSION = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCallback()
        subscribe()
    }

    private fun initCallback() {
        binding.buttReg.setOnClickListener {
            val login = binding.inputLogin.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            val repPass = binding.repeatPass.text.toString().trim()

            viewModel.validateAndRegister(this, login, password, repPass)
        }

        binding.buttonAddAvatar.setOnClickListener {
            checkPermissionsAndProceed()
        }
    }

    private fun subscribe() {
        viewModel.regStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                val username = viewModel.username.value!!
                val password = viewModel.password.value!!
                Log.d("DataBeforeSend", "$username, $password")
                val intent = createRegIntent(username, password, true)
                Log.d("RegIntent", "CreateRegIntent")
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_reg), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.regRespMes.observe(this) { response ->
            response?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, this.getString(R.string.shit_happens), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.validationMessage.observe(this) { message ->
            val finalMessage =
                message.takeIf { !it.isNullOrEmpty() } ?: this.getString(R.string.unknown_error)
            Toast.makeText(this, finalMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                var pictureUri: Uri = Uri.EMPTY
                if (clipData != null) {
                    pictureUri = clipData.getItemAt(0).uri
                } else {
                    result.data?.data?.let { pictureUri = it }
                }
                if (pictureUri != Uri.EMPTY) {
                    viewModel.addProfilePicture(pictureUri)
                }
            }
        }

    private fun checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSION
                )
            } else {
                openImagePicker()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION
                )
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

    companion object {
        const val EXTRA_REG_STATUS = "EXTRA_REG_STATUS"
        const val EXTRA_REG_USERNAME = "EXTRA_REG_USERNAME"
        const val EXTRA_REG_PASSWORD = "EXTRA_REG_PASSWORD"

        private fun createRegIntent(
            username: String,
            password: String,
            isRegSuccess: Boolean
        ): Intent =
            Intent().apply {
                putExtra(EXTRA_REG_USERNAME, username)
                putExtra(EXTRA_REG_PASSWORD, password)
                putExtra(EXTRA_REG_STATUS, isRegSuccess)
                Log.d("CreateIntent", "Intent created")
            }
    }
}