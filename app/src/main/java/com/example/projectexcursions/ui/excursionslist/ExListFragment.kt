package com.example.projectexcursions.ui.excursionslist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectexcursions.R
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.databinding.FragmentExcursionsListBinding
import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.dbs.AppDatabase
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.repositories.ExcursionRepository
import kotlinx.coroutines.launch
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class ExListFragment : Fragment(R.layout.fragment_excursions_list) {

    private lateinit var adapter: ExcursionAdapter
    private lateinit var binding: FragmentExcursionsListBinding

    private val viewModel: ExcursionViewModel by viewModels {
        val apiService = ApiClient.instance
        val dao = AppDatabase.getDatabase(requireContext()).excursionDao()
        val repository = ExcursionRepository(apiService, dao)
        ExcursionViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExcursionsListBinding.bind(view)

        viewModel.loadExcursions(page = 0, size = 10)
        initCallback()
        subscribe()
        fetchExcursions()
    }

    private fun fetchExcursions() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getExcursions(page = 0, size = 10)
                val excursions = response.body()?.content
                if (excursions != null) {
                    adapter.updateData(excursions.map {
                        Excursion(it.id, it.title, it.description)
                    })
                } else {
                    Toast.makeText(requireContext(), "Экскурсии умерли", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Экскурсии умерли по пути из-за: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.excursions.collect { excursions ->
                    adapter.updateData(excursions.map {
                        Excursion(it.id, it.title, it.description)
                    })
                }
            }
        }
    }

    private fun initCallback() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ExcursionAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { excursion ->
            Toast.makeText(
                requireContext(), excursion.title + ": "
                        + excursion.description, Toast.LENGTH_SHORT).show()
        }
    }
}