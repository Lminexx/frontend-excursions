package com.example.projectexcursions.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.UsernameNotFoundException
import com.example.projectexcursions.databinding.FragmentProfileBinding
import com.example.projectexcursions.ui.create_excursion.CreateExcursionActivity
import com.example.projectexcursions.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCallback()
        subscribe()
    }

    private fun initCallback() {
        binding.buttCreateExcursion.setOnClickListener { viewModel.clickCreateExcursion() }
        binding.buttLogOut.setOnClickListener { viewModel.clickComeBack() }
    }

    private fun subscribe() {
        viewModel.wantCreate.observe(viewLifecycleOwner) {wannaCreate ->
            if (wannaCreate) {
                startActivity(Intent(requireContext(), CreateExcursionActivity::class.java))
                viewModel.isCreating()
            }
        }

        viewModel.username.observe(viewLifecycleOwner) {
            username -> binding.userNicknameTextView.text = username ?: throw UsernameNotFoundException("Usera net v tokene")
        }

        viewModel.wantComeBack.observe(viewLifecycleOwner) {wannaLogOut ->
            if (wannaLogOut) {
                viewModel.logout()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                viewModel.cameBack()
            }
        }
    }
}