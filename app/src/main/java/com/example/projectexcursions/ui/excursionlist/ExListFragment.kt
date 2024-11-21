package com.example.projectexcursions.ui.excursionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.databases.OpenWorldDB
import com.example.projectexcursions.databinding.FragmentExcursionsListBinding
import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.ui.excursionslist.ExListViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExListFragment : Fragment(R.layout.fragment_excursions_list) {

    private lateinit var binding: FragmentExcursionsListBinding
    private lateinit var adapter: ExcursionAdapter
    private val viewModel: ExListViewModel by viewModels {
        val apiClient = ApiClient
        val dao = OpenWorldDB.getDatabase(requireContext()).excursionDao()
        val repository = ExcursionRepository(apiClient.instance, dao)
        ExListViewModelFactory(apiClient, repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExcursionsListBinding.inflate(inflater, container, false)

        initCallback()
        subscribe()

        return binding.root
    }

    private fun initCallback() {
        adapter = ExcursionAdapter { excursion ->
            Toast.makeText(
                requireContext(),
                "Вы выбрали экскурсию: ${excursion.title}\n ${excursion.description}",
                Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.excursionsFromApi.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}