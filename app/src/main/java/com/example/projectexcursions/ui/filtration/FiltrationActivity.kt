package com.example.projectexcursions.ui.filtration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projectexcursions.R
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

    private fun initData() {

    }

    private fun subscribe() {
        viewModel.filter.observe(this) { filter ->
            if (filter) {
                finish()
            }
        }
    }

    private fun initCallback() {
        binding.buttonFiltration.setOnClickListener {
            val rating = binding.filtrationRatingValue.text.toString()
            val startDate = binding.filtrationApprovedAtStart.text.toString()
            val endDate = binding.filtrationApprovedAtEnd.text.toString()
            val minDuration = binding.filtrationDurationStart.text.toString()
            val maxDuration = binding.filtrationDurationEnd.text.toString()
            val topic = binding.topicValue.selectedItem.toString()
            val city = binding.filtrationCityValue.text.toString()
            val selectedTags = mutableListOf<String>()
            for (i in 0 until binding.tagsChips.childCount) {
                val chip = binding.tagsChips.getChildAt(i) as? Chip
                chip?.let { selectedTags.add(it.text.toString()) }
            }
            setResult(RESULT_OK, Intent().apply {
                putExtra("rating", rating)
                putExtra("start_date", startDate)
                putExtra("end_date", endDate)
                putExtra("tags", ArrayList(selectedTags))
                putExtra("min_duration", minDuration)
                putExtra("max_duration", maxDuration)
                putExtra("topic", topic)
                putExtra("city", city)
            })
            viewModel.clickFilter()
        }

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