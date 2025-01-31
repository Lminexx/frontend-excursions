package com.example.projectexcursions.ui.mine_excursion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.databinding.MineActivityExcursionBinding
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.mine_excursion.MineExcursionActivity.Companion.createMineExcursionActivityIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MineExcursionActivity : AppCompatActivity() {

    private val viewModel: MineExcursionViewModel by viewModels()

    private lateinit var binding: MineActivityExcursionBinding

    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MineActivityExcursionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCallback()
        initData()
        subscribe()
    }

    private fun initData() {
        val excursionId = intent.getLongExtra(EXTRA_EXCURSION_ID, -1)
        if (excursionId == -1L) {
            Toast.makeText(this, this.getString(R.string.invalid_excursion), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showShimmer()

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
                hideShimmer()
                binding.excursionTitle.text = excursion.title
                binding.excursionAuthor.text = excursion.username
                binding.excursionDescription.text = excursion.description
            } else {
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT).show()
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
                        this@MineExcursionActivity,
                        this@MineExcursionActivity.getString(R.string.error_favorite),
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
                        this@MineExcursionActivity,
                        this@MineExcursionActivity.getString(R.string.error_delete_excursion),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (!viewModel.checkAuthStatus()) {
                    Toast.makeText(
                        this@MineExcursionActivity,
                        this@MineExcursionActivity.getString(R.string.error_favorite),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    startActivity(Intent(this@MineExcursionActivity, MainActivity::class.java))
                    viewModel.clickComeback()
                    viewModel.deleteExcursion()
                }
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.excursionTitle.visibility = View.GONE
        binding.excursionAuthor.visibility = View.GONE
        binding.excursionDescription.visibility = View.GONE
        binding.deleteExcursion.visibility = View.GONE
        binding.favoriteButton.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.excursionTitle.visibility = View.VISIBLE
        binding.excursionAuthor.visibility = View.VISIBLE
        binding.excursionDescription.visibility = View.VISIBLE
        binding.deleteExcursion.visibility = View.VISIBLE
        binding.favoriteButton.visibility = View.VISIBLE
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"

        internal fun Context.createMineExcursionActivityIntent(excursionId: Long): Intent =
            Intent(this, MineExcursionActivity::class.java)
                .putExtra(EXTRA_EXCURSION_ID, excursionId)
    }
}