package com.example.projectexcursions.ui.registration

import android.app.Activity
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
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@AndroidEntryPoint
class RegActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRegBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: RegViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGso()
        initCallback()
        subscribe()
    }

    override fun onPause() {
        super.onPause()

        binding.inputLogin.text.clear()
        binding.inputPass.text.clear()
        binding.repeatPass.text.clear()
    }

    private fun initGso() {
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
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

        binding.buttonGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun subscribe() {
        viewModel.regStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                val username = viewModel.username.value!!
                val password = viewModel.password.value!!
                val avatar = viewModel.avatar.value ?: resourceUri(R.drawable.ic_app_v3)
                Log.d("DataBeforeSend", "$username, $password")
                val intent = createRegIntent(username, password, avatar, true)
                Log.d("RegIntent", "CreateRegIntent")
                setResult(Activity.RESULT_OK, intent)
                finish()
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

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val username = user?.displayName ?: user?.email ?: "unknown"
                    val avatar = user?.photoUrl ?: resourceUri(R.drawable.ic_app_v3)
                    val token = account.idToken
                    Log.d("FirebaseToken", token ?: "ZeroToken")

                    viewModel.registerWithGoogle(username, avatar)

                } else {
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }
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