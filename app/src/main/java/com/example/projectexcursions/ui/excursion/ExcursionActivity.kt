package com.example.projectexcursions.ui.excursion

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.PhotoAdapter
import com.example.projectexcursions.databinding.ActivityExcursionBinding
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExcursionActivity : AppCompatActivity() {

    private val viewModel: ExcursionViewModel by viewModels()
    private lateinit var binding: ActivityExcursionBinding
    private lateinit var adapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcursionBinding.inflate(layoutInflater)
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

        adapter = PhotoAdapter(this, listOf())
        binding.recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages.adapter = adapter

        showShimmer()

        viewModel.loadExcursion(excursionId)
        viewModel.loadPhotos(excursionId)

        binding.excursionDescription.movementMethod = ScrollingMovementMethod()
    }

    private fun subscribe() {
        viewModel.wantComeBack.observe(this) { wannaComeback ->
            if (wannaComeback)
                viewModel.cameBack()
        }

        viewModel.excursion.observe(this) { excursion ->
            if (excursion != null) {
                hideShimmer()
                binding.excursionTitle.text = excursion.title
                binding.excursionAuthor.text = excursion.user.username
                binding.excursionDescription.text = excursion.description
                val url = excursion.user.url
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_app_v3)
                    .error(R.drawable.ic_app_v3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            e?.printStackTrace()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.userAvatar)
                if (viewModel.excursion.value!!.favorite)
                    viewModel.fav()
                else
                    viewModel.notFav()
            } else {
                Toast.makeText(this, this.getString(R.string.excursion_eaten), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.favorite.observe(this) { favorite ->
            if (favorite) {
                binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_fill)
            } else {
                binding.favoriteButton.setBackgroundResource(R.drawable.ic_ex_fav_hollow)
            }
        }

        viewModel.photos.observe(this) { photos ->
            adapter.updatePhotos(photos)
        }
    }

    private fun initCallback() {
        binding.favoriteButton.setOnClickListener {
            it.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                it.isClickable = true
            }, 1000)
            lifecycleScope.launch {
                if (viewModel.checkAuthStatus()) {
                    viewModel.clickFavorite()
                } else {
                    Toast.makeText(this@ExcursionActivity, this@ExcursionActivity.getString(R.string.error_favorite), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.excursionTitle.visibility = View.GONE
        binding.authorContainer.visibility = View.GONE
        binding.excursionDescription.visibility = View.GONE
        binding.favoriteButton.visibility = View.GONE
        binding.recyclerViewImages.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.excursionTitle.visibility = View.VISIBLE
        binding.authorContainer.visibility = View.VISIBLE
        binding.excursionDescription.visibility = View.VISIBLE
        binding.favoriteButton.visibility = View.VISIBLE
        binding.recyclerViewImages.visibility = View.VISIBLE
    }

    companion object {
        private const val EXTRA_EXCURSION_ID = "EXTRA_EXCURSION_ID"

        internal fun Context.createExcursionActivityIntent(excursionId: Long): Intent =
            Intent(this, ExcursionActivity::class.java)
                .putExtra(EXTRA_EXCURSION_ID, excursionId)
    }
}