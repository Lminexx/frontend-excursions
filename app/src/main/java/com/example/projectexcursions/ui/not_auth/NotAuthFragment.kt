package com.example.projectexcursions.ui.not_auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.NotAuthFragmentBinding
import com.example.projectexcursions.ui.auth.AuthActivity
import com.example.projectexcursions.ui.registration.RegActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotAuthFragment : Fragment(R.layout.not_auth_fragment) {

    private lateinit var binding: NotAuthFragmentBinding
    private val viewModel: NotAuthViewModel by viewModels()

    private var prevFrag: String? = null
    private var AUTH_REQUEST_CODE: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AUTH_REQUEST_CODE = arguments?.getInt("AUTH_REQUEST_CODE")
        prevFrag = arguments?.getString("prev_frag")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NotAuthFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCallback()
        subscribe()
    }

    private fun initCallback() {
        binding.open.setOnClickListener { viewModel.wantAuth() }
    }

    private fun subscribe() {
        viewModel.wantAuth.observe(viewLifecycleOwner) { wannaAuth ->
            if (wannaAuth) {
                val intent = Intent(requireContext(), AuthActivity::class.java).putExtra("prev_frag", prevFrag)
                requireActivity().startActivityForResult(intent, 1001)
            }
        }
    }
}