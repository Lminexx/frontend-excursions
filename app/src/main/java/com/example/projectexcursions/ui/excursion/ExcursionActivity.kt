package com.example.projectexcursions.ui.excursion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    private val viewModel: ExcursionViewModel by viewModels()

    private lateinit var binding: ActivityExcursionBinding

    private lateinit var excursion: Excursion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initCallback()
        subscribe()
    }

    private fun initData() {
        excursion = intent.getParcelableExtra<Excursion>("EXTRA_EXCURSION")
            ?: run {
                Toast.makeText(this, "Эксукрсию съели", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        binding.excursionTitle.text = excursion.title
        binding.excursionDescription.text = excursion.description
        binding.excursionAuthor.text = excursion.userId?.toString() ?: "Автора съели"
    }

    private fun subscribe() {
        viewModel.comeBackToMainActivity.observe(this) { wannaComeback ->
            if (wannaComeback) viewModel.cameBack()
        }
    }

    private fun initCallback() {
        binding.buttonComeback.setOnClickListener {
            startActivity(Intent(this@ExcursionActivity, MainActivity::class.java))
            viewModel.clickComeback()
        }
    }
}
