package com.example.projectexcursions.ui.excursionlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.databinding.FragmentExcursionsListBinding
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.ui.excursion.ExcursionActivity.Companion.createExcursionActivityIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExListFragment: Fragment(R.layout.fragment_excursions_list) {

    private lateinit var binding: FragmentExcursionsListBinding
    @Inject
    lateinit var adapter: ExcursionAdapter
    private val viewModel: ExListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExcursionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCallback()
        subscribe()
    }

    private fun initCallback() {
        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener {
            override fun onExcursionClick(excursionsList: ExcursionsList) {
                viewModel.clickExcursion(excursionsList)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.excursions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                Log.d("GetAllExcursions", "All excursions was get")
            }
        }

        viewModel.goToExcursion.observe(viewLifecycleOwner) { wantGoToEx ->
            if (wantGoToEx) {
                val excursion = viewModel.selectedExcursionsList
                if (excursion != null) {
                    val intent = requireContext().createExcursionActivityIntent(excursionId = excursion.id)
                    startActivity(intent)
                    viewModel.goneToExcursion()
                }
            }
        }
    }
}
