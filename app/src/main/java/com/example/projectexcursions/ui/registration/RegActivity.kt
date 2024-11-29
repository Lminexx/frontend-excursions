package com.example.projectexcursions.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

            viewModel.validateAndRegister(login, password, repPass)
        }
        binding.buttComeBack.setOnClickListener { viewModel.clickComeBack() }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) {wannaComeBack ->
            if (wannaComeBack) {
                startActivity(Intent(this@RegActivity, AuthActivity::class.java))
                viewModel.cameBack()
            }
        }

        viewModel.regStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                startActivity(Intent(this, AuthActivity::class.java))
            }
        }

        viewModel.regRespMes.observe(this) { response ->
            response?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.validationMessage.observe(this) { message ->
            val finalMessage = message.takeIf { !it.isNullOrEmpty() } ?: "Неизвестная ошибка"
            Toast.makeText(this, finalMessage, Toast.LENGTH_SHORT).show()
        }
    }
}