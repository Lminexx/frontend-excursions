package com.example.projectexcursions.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.UsernameNotFoundException
import com.example.projectexcursions.databinding.FragmentProfileBinding
import com.example.projectexcursions.ui.create_excursion.CreateExcursionActivity
import com.example.projectexcursions.ui.created_excursions_list.CreatedExListActivity
import com.example.projectexcursions.ui.main.MainActivity
import com.example.projectexcursions.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val mainViewModel: MainViewModel by activityViewModels()
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

        val uri = viewModel.getAvatar()
        if (uri.toString() != null.toString()) {
            binding.userPhoto.setImageURI(uri)
        } else{
            binding.userPhoto.setBackgroundResource(R.drawable.ic_app_v3)
        }
    }

    private fun initCallback() {
        binding.buttCreateExcursion.setOnClickListener { viewModel.clickCreateExcursion() }
        binding.buttLogOut.setOnClickListener { viewModel.clickComeBack() }
        binding.createdExcursionsList.setOnClickListener { viewModel.createdExcsList() }
    }

    private fun subscribe() {
        viewModel.wantCreate.observe(viewLifecycleOwner) { wannaCreate ->
            if (wannaCreate) {
                startActivity(Intent(requireContext(), CreateExcursionActivity::class.java))
                viewModel.isCreating()
            }
        }

        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.userNickname.text =
                username ?: throw UsernameNotFoundException("Usera net v tokene")
        }

        viewModel.wantComeBack.observe(viewLifecycleOwner) { wannaLogOut ->
            if (wannaLogOut) {
                Log.d("WantLogOut", "true")
                viewModel.logout()
                (requireActivity() as? MainActivity)?.updateBottomNavSelectionToList()
            }
        }

        viewModel.goToCreatedExcs.observe(viewLifecycleOwner) { wannaCreated ->
            if (wannaCreated) {
                startActivity(Intent(requireContext(), CreatedExListActivity::class.java))
            }
        }
    }
}