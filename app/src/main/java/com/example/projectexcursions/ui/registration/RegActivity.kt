package com.example.projectexcursions.ui.registration

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityRegBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRegBinding
    private val viewModel: RegViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCallback()
        subscribe()
    }

    override fun onPause() {
        super.onPause()

        binding.inputLogin.text.clear()
        binding.inputPass.text.clear()
        binding.repeatPass.text.clear()
    }

    private fun initCallback() {
        binding.buttReg.setOnClickListener {
            val login = binding.inputLogin.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            val repPass = binding.repeatPass.text.toString().trim()

            viewModel.validateAndRegister(this, login, password, repPass)
        }

        binding.buttonAddAvatar.setOnClickListener {
            openImagePicker()
        }
    }

    private fun subscribe() {
        viewModel.regStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                val username = viewModel.username.value!!
                val password = viewModel.password.value!!
                val avatar = viewModel.avatar.value!!
                Log.d("DataBeforeSend", "$username, $password")
                val intent = createRegIntent(username, password, avatar, true)
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

        viewModel.avatar.observe(this) { picture ->
            binding.buttonAddAvatar.setImageURI(picture)
            binding.buttonAddAvatar.alpha = 1F
        }

    }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                var pictureUri: Uri? = data?.data
                if (pictureUri != null) {
                    viewModel.addProfilePicture(pictureUri)
                } else {
                    result.data?.data?.let { pictureUri = it }
                }
            }
        }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImages.launch(intent)
    }

    companion object {
        const val EXTRA_REG_STATUS = "EXTRA_REG_STATUS"
        const val EXTRA_REG_USERNAME = "EXTRA_REG_USERNAME"
        const val EXTRA_REG_PASSWORD = "EXTRA_REG_PASSWORD"
        const val EXTRA_REG_AVATAR = "EXTRA_REG_AVATAR"

        private fun createRegIntent(
            username: String,
            password: String,
            avatar: Uri,
            isRegSuccess: Boolean
        ): Intent =
            Intent().apply {
                putExtra(EXTRA_REG_USERNAME, username)
                putExtra(EXTRA_REG_PASSWORD, password)
                putExtra(EXTRA_REG_AVATAR, avatar)
                putExtra(EXTRA_REG_STATUS, isRegSuccess)
                Log.d("CreateIntent", "Intent created")
            }
    }
}