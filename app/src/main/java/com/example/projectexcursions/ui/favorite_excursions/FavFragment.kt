package com.example.projectexcursions.ui.favorite_excursions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
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
class FavFragment : Fragment(R.layout.excursions_list) {

    @Inject
    lateinit var adapter: ExcursionAdapter
    private val viewModel: FavViewModel by viewModels()
    private lateinit var errorContainer: ErrorBinding
    private lateinit var binding: ExcursionsListBinding
    private lateinit var animation: Animation
    private lateinit var emptyListContainer: EmptyListBinding

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

    override fun onResume() {
        super.onResume()

        adapter.refresh()
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

    private fun initCallback(){

        errorContainer.retryButton.setOnClickListener {
            adapter.retry()
        }

        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener {
            override fun onExcursionClick(excursionsList: ExcursionsList) {
                viewModel.clickExcursion(excursionsList)
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
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.favExcursions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                Log.d("GetAllExcursions", "All excursions was get")
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
                        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.appear_pop_up)
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
                    animation = AnimationUtils.loadAnimation(requireContext(), R.anim.appear_pop_up)
                    errorContainer.errorLayout.visibility = View.VISIBLE
                    errorContainer.errorLayout.startAnimation(animation)
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