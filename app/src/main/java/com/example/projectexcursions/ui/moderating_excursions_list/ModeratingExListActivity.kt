package com.example.projectexcursions.ui.moderating_excursions_list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.databinding.EmptyListBinding
import com.example.projectexcursions.databinding.ErrorBinding
import com.example.projectexcursions.databinding.ExcursionsListBinding
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.ui.excursion.ExcursionActivity.Companion.createExcursionActivityIntent
import com.example.projectexcursions.utilies.ExcursionsListException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ModeratingExListActivity: AppCompatActivity() {

    @Inject
    lateinit var adapter: ExcursionAdapter
    private lateinit var animation: Animation
    private lateinit var errorContainer: ErrorBinding
    private lateinit var binding: ExcursionsListBinding
    private lateinit var emptyListContainer: EmptyListBinding
    private val viewModel: ModeratingExListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExcursionsListBinding.inflate(layoutInflater)
        errorContainer = binding.errorContainer
        emptyListContainer = binding.emptyListContainer
        setContentView(binding.root)

        initData()
        initCallback()
        subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()

        hideShimmer()
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.moderatingExcursions.collectLatest { pagingData ->
                Log.d("excursions", "$pagingData")
                adapter.submitData(pagingData)
                Log.d("GetAllExcursions", "${viewModel.moderatingExcursions}")
            }
        }
    }

    private fun initCallback() {
        errorContainer.retryButton.setOnClickListener {
            adapter.retry()
        }

        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener{
            override fun onExcursionClick(excursionsList: ExcursionsList) {
                viewModel.clickExcursion(excursionsList)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun initData() {
        showShimmer()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.searchView.visibility = View.GONE
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.moderatingExcursions.collectLatest { pagingData ->
                Log.d("excursions", "$pagingData")
                adapter.submitData(pagingData)
                Log.d("GetAllExcursions", "${viewModel.moderatingExcursions}")
            }
        }

        adapter.addLoadStateListener { loadState ->
            binding.swipeRefresh.isRefreshing = loadState.source.refresh is LoadState.Loading
            val isEmptyList = adapter.itemCount == 0
            when (loadState.source.refresh) {
                is LoadState.Loading -> {
                    showShimmer()
                }
                is LoadState.NotLoading -> {
                    hideShimmer()
                    if (isEmptyList) {
                        binding.recyclerView.visibility = View.GONE
                        animation = AnimationUtils.loadAnimation(this, R.anim.appear_pop_up)
                        emptyListContainer.emptyListLayout.visibility = View.VISIBLE
                        emptyListContainer.emptyListLayout.startAnimation(animation)
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        emptyListContainer.emptyListLayout.visibility = View.GONE
                    }
                }
                is LoadState.Error -> {
                    hideShimmer()
                    binding.recyclerView.visibility = View.GONE
                    binding.filterButton.visibility = View.GONE
                    animation = AnimationUtils.loadAnimation(this, R.anim.appear_pop_up)
                    errorContainer.errorLayout.visibility = View.VISIBLE
                    errorContainer.errorLayout.startAnimation(animation)
                }
            }
        }

        viewModel.goToExcursion.observe(this) { wantGoToEx ->
            if (wantGoToEx) {
                val excursion = viewModel.selectedExcursionsList
                if (excursion != null) {
                    val intent = this.createExcursionActivityIntent(excursionId = excursion.id, true)
                    moderatingExcursionLauncher.launch(intent)
                    viewModel.goneToExcursion()
                }
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private val moderatingExcursionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            adapter.refresh()
        }
    }
}