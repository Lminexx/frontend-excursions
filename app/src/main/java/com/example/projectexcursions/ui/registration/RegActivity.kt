package com.example.projectexcursions.ui.registration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.example.projectexcursions.ui.auth.AuthActivity
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

    private fun initCallback() {
        binding.buttReg.setOnClickListener {
            val login = binding.inputLogin.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            val repPass = binding.repeatPass.text.toString().trim()

            viewModel.validateAndRegister(this, login, password, repPass)
        }
        binding.buttComeBack.setOnClickListener { viewModel.clickComeBack() }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) {wannaComeBack ->
            if (wannaComeBack) {
                startActivity(Intent(this@RegActivity, AuthActivity::class.java))
                viewModel.cameBack()
                finish()
            }
        }

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
                Toast.makeText(this, this.getString(R.string.shit_happens), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.validationMessage.observe(this) { message ->
            val finalMessage = message.takeIf { !it.isNullOrEmpty() } ?: this.getString(R.string.unknown_error)
            Toast.makeText(this, finalMessage, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_REG_STATUS = "EXTRA_REG_STATUS"
        const val EXTRA_REG_USERNAME = "EXTRA_REG_USERNAME"
        const val EXTRA_REG_PASSWORD = "EXTRA_REG_PASSWORD"

        private fun createRegIntent(username: String, password: String, isRegSuccess: Boolean): Intent =
            Intent().apply {
                putExtra(EXTRA_REG_USERNAME, username)
                putExtra(EXTRA_REG_PASSWORD, password)
                putExtra(EXTRA_REG_STATUS, isRegSuccess)
                Log.d("CreateIntent", "Intent created")
            }
    }
}