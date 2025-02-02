package com.example.projectexcursions.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityAuthBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.registration.RegActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    private val REG_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)

        setResult(Activity.RESULT_CANCELED)
        setContentView(binding.root)
        initCallback()
        subscribe()
    }

    private fun subscribe() {
        viewModel.loginStatus.observe(this) { successAuth ->
            if (successAuth) {
                val prevFrag = intent.getStringExtra("prev_frag")
                Log.d("AuthActivity", "Success auth, prev_frag: $prevFrag")

                val resultIntent = createAuthResultIntent(true)
                resultIntent.putExtra("prev_frag", prevFrag)

                Log.d("AuthActivity", "Setting result OK")
                setResult(Activity.RESULT_OK, resultIntent)

                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_auth), Toast.LENGTH_SHORT).show()
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val isReg = data?.getBooleanExtra(RegActivity.EXTRA_REG_STATUS, false) ?: false
            Log.d("DataNaN?", "$data")
            if (isReg) {
                Log.d("RegIntent", "GetRegIntent")
                val username = data?.getStringExtra(RegActivity.EXTRA_REG_USERNAME)
                val password = data?.getStringExtra(RegActivity.EXTRA_REG_PASSWORD)
                Log.d("Data", "$username")
                Log.d("Data", "$password")
                viewModel.validateAndLogin(this, username!!, password!!)
            }
        }
    }

    companion object {
        const val EXTRA_AUTH_STATUS = "EXTRA_AUTH_STATUS"

         private fun createAuthResultIntent(isAuthSuccess: Boolean): Intent =
            Intent().putExtra(EXTRA_AUTH_STATUS, isAuthSuccess)
    }
}