package com.example.projectexcursions.ui.registration

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.example.projectexcursions.utilies.Blur
import com.example.projectexcursions.utilies.CustomProgressBar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegActivity: AppCompatActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityRegBinding
    private val viewModel: RegViewModel by viewModels()
    private val progressBar = CustomProgressBar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCallback()
        subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressBar.dialog?.takeIf { it.isShowing }?.dismiss()
        unblur()
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
                blur()
                progressBar.show(this)
                val username = viewModel.username.value!!
                val password = viewModel.password.value!!
                val avatar = viewModel.avatar.value ?: resourceUri(R.drawable.ic_app_v3)
                Log.d("DataBeforeSend", "$username, $password")
                lifecycleScope.launch {
                    delay(1000)
                    val intent = createRegIntent(username, password, avatar, true)
                    Log.d("RegIntent", "CreateRegIntent")
                    setResult(RESULT_OK, intent)
                    progressBar.dialog.dismiss()
                    unblur()
                    finish()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_reg), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    private fun Context.resourceUri(resourceId: Int): Uri = with(resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(resourceId))
            .appendPath(getResourceTypeName(resourceId))
            .appendPath(getResourceEntryName(resourceId))
            .build()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImages.launch(intent)
    }

    fun blur() {
        Blur().blur(this, 12, 3, binding.parentLayout)
    }

    fun unblur() {
        Blur().unblur(binding.parentLayout)
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