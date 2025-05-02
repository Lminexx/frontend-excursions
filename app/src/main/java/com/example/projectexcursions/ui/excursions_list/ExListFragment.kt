package com.example.projectexcursions.ui.excursions_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.databinding.ErrorBinding
import com.example.projectexcursions.databinding.ExcursionsListBinding
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.ui.excursion.ExcursionActivity.Companion.createExcursionActivityIntent
import com.example.projectexcursions.ui.excursions_list.ExListViewModel
import com.example.projectexcursions.ui.filtration.FiltrationActivity
import com.example.projectexcursions.utilies.ExcursionsListException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExListFragment : Fragment(R.layout.excursions_list) {

    @Inject
    lateinit var adapter: ExcursionAdapter
    private lateinit var errorContainer: ErrorBinding
    private lateinit var binding: ExcursionsListBinding
    private val viewModel: ExListViewModel by viewModels()

    private val filterLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val rating = data?.getStringExtra("rating")?.toFloatOrNull()
            val startDate = data?.getStringExtra("start_date")
            val endDate = data?.getStringExtra("end_date")
            val tags = data?.getStringArrayListExtra("tags") ?: emptyList()
            val minDuration = data?.getStringExtra("min_duration")?.toIntOrNull()
            val maxDuration = data?.getStringExtra("max_duration")?.toIntOrNull()
            val topic = data?.getStringExtra("topic")
            val city = data?.getStringExtra("city")


            viewModel.setFiltrationData(rating, startDate, endDate, tags, minDuration, maxDuration, topic,city)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExcursionsListBinding.inflate(inflater, container, false)
        errorContainer = binding.errorContainer

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initCallback()
        subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.shimmerLayout.stopShimmer()
    }

    private fun initData() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        showShimmer()
    }

    private fun initCallback() {

        errorContainer.retryButton.setOnClickListener {
            adapter.retry()
        }

        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener {
            override fun onExcursionClick(excursionsList: ExcursionsList) {
                viewModel.clickExcursion(excursionsList)
                Log.d("BindExcursion", excursionsList.title)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        binding.filterButton.setOnClickListener{
            val intent = Intent(requireContext(), FiltrationActivity::class.java)
            filterLauncher.launch(intent)
        }
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.excursions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            viewModel.filterExcursions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
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
                    errorContainer.errorMessage.text = LoadState.Error(ExcursionsListException()).toString()
                }
            }
        }

        viewModel.goToExcursion.observe(viewLifecycleOwner) { wantGoToEx ->
            if (wantGoToEx) {
                val excursion = viewModel.selectedExcursionsList
                if (excursion != null) {
                    val intent = requireContext().createExcursionActivityIntent(excursionId = excursion.id, false)
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