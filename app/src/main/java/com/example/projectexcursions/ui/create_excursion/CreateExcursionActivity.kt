package com.example.projectexcursions.ui.create_excursion

import android.content.Intent
import android.os.Bundle
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
        binding.buttonCreateExcursion.setOnClickListener { viewModel.clickCreateExcursion() }
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) {wannaComeBack ->
            if (wannaComeBack) {
                startActivity(Intent(this@CreateExcursionActivity, MainActivity::class.java))
                viewModel.cameBack()
            }
        }

        viewModel.wantCreateExc.observe(this) {wannaCreate ->
            if (wannaCreate) {}
        }

        viewModel.createExcursion.observe(this) {isSuccessful ->
            if (isSuccessful) {
                startActivity(Intent(this@CreateExcursionActivity, MainActivity::class.java))
                viewModel.excursionCreated()
            }
        }
    }
}