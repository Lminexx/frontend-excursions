package com.example.projectexcursions.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityAuthBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.registration.RegActivity


class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    saveToken(token)
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

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("auth_token", token).apply()
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