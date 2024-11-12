package com.example.projectexcursions.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityRegBinding
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.user.User

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

            val userLogin: EditText = findViewById(R.id.input_login)
            val userPassword: EditText = findViewById(R.id.input_pass)
            val userRepeatPass: EditText = findViewById(R.id.repeat_pass)
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()
            val repPass = userRepeatPass.text.toString().trim()

            if (login == "" || login == " ")
                Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show()
            else if (password == "" || password == " ")
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
            else if (repPass == "" || repPass == " ")
                Toast.makeText(this, "Повторите пароль", Toast.LENGTH_SHORT).show()
            else if (password != repPass)
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            else {
                viewModel.reg(login, password)
            }
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

        viewModel.inputLogin.observe(this) {login ->
            println(login)
        }
    }
}