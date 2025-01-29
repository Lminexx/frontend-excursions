package com.example.projectexcursions.ui.excursion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    private val viewModel: ExcursionViewModel by viewModels()

    private lateinit var binding: ActivityExcursionBinding

    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        subscribe()
    }

    private fun initData() {
        val excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)
        if (excursionId == -1L) {
            Toast.makeText(this, this.getString(R.string.invalid_excursion), Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }
        viewModel.loadExcursion(excursionId)
        if (viewModel.excursion.value?.favorite == true) {
            binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_fill)
        }
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
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.favorite.observe(this) { favorite ->
            if (favorite) {
                viewModel.addFavorite()
            } else {
                viewModel.deleteFavorite()
            }
        }

    }

    private fun initCallback() {
        binding.buttonComeback.setOnClickListener {
            startActivity(Intent(this@ExcursionActivity, MainActivity::class.java))
            viewModel.clickComeback()
        }
        binding.favoriteButton.setOnClickListener {
            lifecycleScope.launch {
                if (viewModel.checkAuthStatus()) {
                    if (isFavorite) {
                        binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_hollow)
                        isFavorite = false
                        viewModel.clickNotFavorite()
                    } else {
                        binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_fill)
                        isFavorite = true
                        viewModel.clickFavorite()
                    }
                } else {
                    Toast.makeText(
                        this@ExcursionActivity,
                        this@ExcursionActivity.getString(R.string.error_favorite),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
        binding.deleteExcursion.setOnClickListener {
            lifecycleScope.launch {
                if (viewModel.username.value != viewModel.excursion.value?.username) {
                    Toast.makeText(
                        this@ExcursionActivity,
                        this@ExcursionActivity.getString(R.string.error_delete_excursion),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (!viewModel.checkAuthStatus()) {
                    Toast.makeText(
                        this@ExcursionActivity,
                        this@ExcursionActivity.getString(R.string.error_favorite),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    startActivity(Intent(this@ExcursionActivity, MainActivity::class.java))
                    viewModel.clickComeback()
                    viewModel.deleteExcursion()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"

        internal fun Context.createExcursionActivityIntent(excursionId: Long): Intent =
            Intent(this, ExcursionActivity::class.java)
                .putExtra(EXTRA_EXCURSION_ID, excursionId)
    }
}