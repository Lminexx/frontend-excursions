package com.example.projectexcursions.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.example.projectexcursions.ui.auth.AuthActivity
import com.google.android.material.textfield.TextInputEditText

class RegActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRegBinding
    private val viewModel: RegViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCallback()
        subscribe()

        binding.inputLogin.addTextChangedListener { editable ->
            val loginText = editable?.toString() ?: ""
            viewModel.updateInputLogin(loginText)
        }
    }

    private fun initCallback() {
        binding.buttReg.setOnClickListener { viewModel.clickRegister() }
        binding.buttComeBack.setOnClickListener { viewModel.clickComeBack() }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) {wannaComeBack ->
            if (wannaComeBack) {
                startActivity(Intent(this@RegActivity, AuthActivity::class.java))
                viewModel.cameBack()
            }
        }
        viewModel.inputLogin.observe(this) {login ->
            println(login)
        }
    }
}