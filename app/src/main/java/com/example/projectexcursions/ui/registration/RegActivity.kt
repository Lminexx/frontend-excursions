package com.example.projectexcursions.ui.registration

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityRegBinding

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
        //TODO implement
    }

    private fun subscribe() {
        //todo
    }
}