package com.example.projectexcursions.ui.created_excursions_list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.databinding.ExcursionsListBinding
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.ui.excursion.ExcursionActivity.Companion.createExcursionActivityIntent
import com.example.projectexcursions.ui.mine_excursion.MineExcursionActivity.Companion.createMineExcursionActivityIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreatedExListActivity: AppCompatActivity() {

    private lateinit var binding: ExcursionsListBinding
    @Inject
    lateinit var adapter: ExcursionAdapter
    private val viewModel: CreatedExListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExcursionsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initCallback()
        subscribe()
    }

    private fun initCallback() {
        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener{
            override fun onExcursionClick(excursionsList: ExcursionsList) {
                viewModel.clickExcursion(excursionsList)
            }
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchExcursionsQuery(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.searchExcursionsQuery(newText)
                }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            viewModel.resetSearch()
            false
        }

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                lifecycleScope.launch { adapter.submitData(PagingData.empty()) }
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun initData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        showShimmer()
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.createdExcursions.collectLatest { pagingData ->
                Log.d("excursions", "$pagingData")
                adapter.submitData(pagingData)
                Log.d("GetAllExcursions", "${viewModel.createdExcursions}")
            }
        }

        adapter.addLoadStateListener { loadState ->
            binding.swipeRefresh.isRefreshing = loadState.source.refresh is LoadState.Loading
            when (loadState.source.refresh) {
                is LoadState.Loading -> {
                    showShimmer()
                }
                is LoadState.NotLoading -> {
                    hideShimmer()
                }
                is LoadState.Error -> {
                    showShimmer()
                    Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.goToExcursion.observe(this) { wantGoToEx ->
            if (wantGoToEx) {
                val excursion = viewModel.selectedExcursionsList
                if (excursion != null) {
                    val intent = this.createMineExcursionActivityIntent(excursionId = excursion.id)
                    startActivity(intent)
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
}