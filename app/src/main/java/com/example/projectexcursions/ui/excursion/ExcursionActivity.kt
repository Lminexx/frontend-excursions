package com.example.projectexcursions.ui.excursion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    private val viewModel: ExcursionViewModel by viewModels()

    private lateinit var binding: ActivityExcursionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initCallback()
        subscribe()
    }

    private fun initData() {
        val excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)
        if (excursionId == -1L) {
            Toast.makeText(this, this.getString(R.string.invalid_excursion), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.loadExcursion(excursionId)
        binding.excursionDescription.movementMethod = ScrollingMovementMethod()
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeback ->
            if (wannaComeback) viewModel.cameBack()
        }

        viewModel.excursion.observe(this) { excursion ->
            if (excursion != null) {
                binding.excursionTitle.text = excursion.title
                binding.excursionAuthor.text = excursion.username
                binding.excursionDescription.text = excursion.description
            } else {
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initCallback() {
        binding.buttonComeback.setOnClickListener {
            startActivity(Intent(this@ExcursionActivity, MainActivity::class.java))
            viewModel.clickComeback()
        }
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"

        internal fun Context.createExcursionActivityIntent(excursionId: Long): Intent =
            Intent(this, ExcursionActivity::class.java)
                .putExtra(EXTRA_EXCURSION_ID, excursionId)
    }
}