package com.example.projectexcursions.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityAuthBinding
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
        viewModel.loginStatus.observe(this) {
            Toast.makeText(this, viewModel.message.value, Toast.LENGTH_SHORT).show()
        }
        viewModel.wantReg.observe(this) { value ->
            if (value) {
                startActivity(Intent(this@AuthActivity, RegActivity::class.java))
                viewModel.goneToReg()
            }
        }
    }



    private fun initCallback() {
        binding.buttAuth.setOnClickListener { viewModel.clickAuth() }
        binding.buttReg.setOnClickListener { viewModel.clickRegister() }
    }
}