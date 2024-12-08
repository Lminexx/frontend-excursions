package com.example.projectexcursions.ui.create_excursion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import com.example.projectexcursions.ui.main.MainActivity

class CreateExcursionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExcursionCreateBinding
    private val viewModel: CreateExcursionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCallback()
        subscribe()
    }

    private fun initCallback() {
        binding.buttonComeback.setOnClickListener { viewModel.clickComeBack() }
        binding.buttonCreateExcursion.setOnClickListener {
            // Получаем текст из полей ввода и передаем их в метод clickCreateButton.
            val title = binding.excursionTitle.text.toString()
            val description = binding.excursionDescription.text.toString()
            viewModel.clickCreateButton(title, description)
        }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeBack ->
            if (wannaComeBack) {
                startActivity(Intent(this@CreateExcursionActivity, MainActivity::class.java))
                viewModel.cameBack()
            }
        }

        viewModel.createExcursionStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Экскурсия успешно создана", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@CreateExcursionActivity, MainActivity::class.java))
                viewModel.excursionCreated()
            } else {
                // Показываем сообщение об ошибке создания экскурсии.
                Toast.makeText(this, "Ошибка при создании экскурсии", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.validationMessage.observe(this) { message ->
            message?.let {
                // Показ сообщения о валидации пользователю.
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.creationMessage.observe(this) { message ->
            message?.let {
                // Показать успешное или неудачное сообщение о создании.
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
