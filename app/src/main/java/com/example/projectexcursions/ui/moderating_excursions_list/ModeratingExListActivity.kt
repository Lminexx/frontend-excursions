package com.example.projectexcursions.ui.moderating_excursions_list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.databinding.ErrorBinding
import com.example.projectexcursions.databinding.ExcursionsListBinding
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.ui.excursion.ExcursionActivity.Companion.createExcursionActivityIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ModeratingExListActivity: AppCompatActivity() {

    @Inject
    lateinit var adapter: ExcursionAdapter
    private lateinit var errorContainer: ErrorBinding
    private lateinit var binding: ExcursionsListBinding
    private val viewModel: ModeratingExListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExcursionsListBinding.inflate(layoutInflater)
        errorContainer = binding.errorContainer
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
            when (loadState.source.refresh) {
                is LoadState.Loading -> {
                    errorContainer.errorLayout.visibility = View.GONE
                    showShimmer()
                }
                is LoadState.NotLoading -> {
                    hideShimmer()
                    errorContainer.errorLayout.visibility = View.GONE
                }
                is LoadState.Error -> {
                    showShimmer()
                    binding.recyclerView.visibility = View.GONE
                    errorContainer.errorLayout.visibility = View.VISIBLE
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