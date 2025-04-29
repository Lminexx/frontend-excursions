package com.example.projectexcursions.ui.filtration

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.databinding.ActivityFilterBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltrationActivity : AppCompatActivity() {
    private val viewModel: FiltrationViewModel by viewModels()
    private lateinit var binding: ActivityFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCallback()
        initData()
        subscribe()
    }

    private fun initData(){

    }

    private fun subscribe(){

    }

    private fun initCallback(){
        binding.addTagsButton.setOnClickListener {
            addNewChip()
        }
    }

    private fun addNewChip() {
        val keyword: String = binding.addTagsString.text.toString()
        println(keyword)
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите тэг", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val inflater = LayoutInflater.from(this)
            val newChip =
                inflater.inflate(R.layout.layout_chip_entry, binding.tagsChips, false) as Chip
            newChip.text = keyword
            newChip.setCloseIconVisible(true)
            newChip.setChipBackgroundColorResource(R.color.lighter_blue)
            newChip.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tagsChips.addView(newChip)
            newChip.setOnCloseIconClickListener {
                binding.tagsChips.removeView(newChip)
            }
            binding.addTagsString.setText("")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}