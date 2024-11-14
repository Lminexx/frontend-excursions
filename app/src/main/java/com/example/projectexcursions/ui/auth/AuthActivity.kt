package com.example.projectexcursions.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityAuthBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.registration.RegActivity

class AuthActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCallback()
        subscribe()
    }

    private fun subscribe() {
        viewModel.loginStatus.observe(this) {successAuth ->
            when(successAuth) {
                true -> {
                    Toast.makeText(this, "Авторизация успешна", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    viewModel.sucAuth()
                }
                false -> {
                    Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                }
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
        binding.buttAuth.setOnClickListener { viewModel.clickAuth() }
        binding.goToRegButt.setOnClickListener { viewModel.clickRegister() }
    }
}