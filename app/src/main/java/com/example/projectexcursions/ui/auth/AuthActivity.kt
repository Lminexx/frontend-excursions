package com.example.projectexcursions.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.projectexcursions.databinding.ActivityAuthBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.registration.RegActivity
import com.example.projectexcursions.token_bd.TokenRepository

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создание tokenRepository
        val tokenRepository = TokenRepository(this)

        // Создание ViewModel с помощью фабрики
        val factory = AuthViewModelFactory(tokenRepository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        binding.buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        initCallback()
        subscribe()
    }

    private fun subscribe() {
        viewModel.loginStatus.observe(this) { successAuth ->
            if (successAuth) {
                viewModel.token.observe(this) { token ->
                    Log.d("logViewModel", "Нажал кнопочку войти")
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                }
            } else {
                Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.wantReg.observe(this) { wannaReg ->
            if (wannaReg) {
                startActivity(Intent(this@AuthActivity, RegActivity::class.java))
                viewModel.goneToReg()
            }
        }
    }
    private fun initCallback() {
        binding.buttAuth.setOnClickListener {
            val login = binding.inputLogin.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()

            viewModel.validateAndLogin(login, password)
        }
        binding.goToRegButt.setOnClickListener { viewModel.clickRegister() }
    }
}