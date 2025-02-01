package com.example.projectexcursions.ui.create_excursion

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityExcursionCreateBinding
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        binding.buttonCreateExcursion.setOnClickListener { viewModel.clickCreateExcursion() }
        binding.excursionDescription.movementMethod = ScrollingMovementMethod()
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeBack ->
            if (wannaComeBack) {
                finish()
            }
        }

        viewModel.createExcursion.observe(this) { wannaCreate ->
            if (wannaCreate) {
                Log.d("WantCreate", "WantCreate")
                val title = binding.excursionTitle.text.toString().trim()
                val description = binding.excursionDescription.text.toString().trim()
                if (viewModel.isExcursionCorrect(this, title, description)) {
                    viewModel.createExcursion(this@CreateExcursionActivity, title, description)
                }
            }
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}