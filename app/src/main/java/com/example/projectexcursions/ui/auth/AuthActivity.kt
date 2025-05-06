package com.example.projectexcursions.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityAuthBinding
import com.example.projectexcursions.ui.registration.RegActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity: AppCompatActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    private val REG_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)

        initCallback()
        subscribe()
    }

    override fun onPause() {
        super.onPause()

        binding.inputLogin.text.clear()
        binding.inputPass.text.clear()
    }

    private fun subscribe() {
        viewModel.loginStatus.observe(this) { successAuth ->
            if (successAuth) {
                val prevFrag = intent.getStringExtra("prev_frag")
                Log.d("AuthActivity", "Success auth, prev_frag: $prevFrag")
                val role = viewModel.role.value
                val resultIntent = createAuthResultIntent(isAuthSuccess = true, isModerator = role == "MODERATOR" || role == "ADMIN")
                resultIntent.putExtra("prev_frag", prevFrag)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        viewModel.wantReg.observe(this) { wannaReg ->
            if (wannaReg) {
                val intent = Intent(this@AuthActivity, RegActivity::class.java)
                startActivityForResult(intent, REG_REQUEST_CODE)
                viewModel.goneToReg()
            }
        }

        viewModel.wantComeBack.observe(this) { wannaComeBack ->
            if (wannaComeBack) {
                finish()
            }
        }

        viewModel.validationMessage.observe(this) {message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initCallback() {
        binding.buttAuth.setOnClickListener {
            val login = binding.inputLogin.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            viewModel.validateAndLogin(this ,login, password)
        }
        binding.buttReg.setOnClickListener { viewModel.clickRegister() }
        binding.buttComeBack.setOnClickListener { viewModel.clickComeBack() }

        binding.buttonGoogleSignIn.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                firebaseAuth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    signInWithGoogle()
                }
            } else signInWithGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val isReg = data?.getBooleanExtra(RegActivity.EXTRA_REG_STATUS, false) == true
            Log.d("DataNaN?", "$data")
            if (isReg) {
                blur()
                Log.d("RegIntent", "GetRegIntent")
                val username = data.getStringExtra(RegActivity.EXTRA_REG_USERNAME)
                val password = data.getStringExtra(RegActivity.EXTRA_REG_PASSWORD)
                val avatar = data.getParcelableExtra<Uri>(RegActivity.EXTRA_REG_AVATAR)
                if (avatar != null) {
                    viewModel.setAvatar(avatar)
                }
                Log.d("Data", "$username")
                Log.d("Data", "$password")
                Log.d("Data", "$avatar")
                viewModel.validateAndLogin(this, username!!, password!!)
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
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

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        blur()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.getIdToken(true)
                        ?.addOnSuccessListener { result ->
                            val token = result.token
                            Log.d("FirebaseToken", token ?: "ZeroToken")
                            if (token != null) viewModel.firebaseAuth(token)
                        }
                } else {
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun blur() {
        Blurry.with(this)
            .radius(10)
            .sampling(2)
            .async()
            .onto(binding.parentLayout)
    }

    companion object {
        const val EXTRA_AUTH_STATUS = "EXTRA_AUTH_STATUS"
        const val EXTRA_MODERATOR_ROLE = "EXTRA_MODERATOR_ROLE"

         private fun createAuthResultIntent(isAuthSuccess: Boolean, isModerator: Boolean): Intent =
            Intent().apply {
                putExtra(EXTRA_AUTH_STATUS, isAuthSuccess)
                putExtra(EXTRA_MODERATOR_ROLE, isModerator)
            }
    }
}