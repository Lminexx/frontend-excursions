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
import com.example.projectexcursions.databinding.FragmentExcursionsListBinding
import com.example.projectexcursions.models.Excursion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExListFragment : Fragment(R.layout.fragment_excursions_list) {

    private lateinit var binding: FragmentExcursionsListBinding

    @Inject
    lateinit var adapter: ExcursionAdapter

    private val viewModel: ExListViewModel by viewModels()

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
        adapter.onExcursionClickListener = object : ExcursionAdapter.OnExcursionClickListener{
            override fun onExcursionClick(excursion: Excursion) {
                Toast.makeText(requireContext(),
                    "Мы работаем над открытием ${excursion.title}: \n${excursion.description}",
                    Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun subscribe() {
        lifecycleScope.launch {
            viewModel.excursions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}