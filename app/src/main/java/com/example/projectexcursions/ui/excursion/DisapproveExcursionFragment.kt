package com.example.projectexcursions.ui.excursion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.DisapproveExcurusionBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisapproveExcursionFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DisapproveExcurusionBottomSheetBinding

    private val viewModel: ExcursionViewModel by activityViewModels()

    private val id: Long get() = arguments?.getLong(ID) ?: -1L

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DisapproveExcurusionBottomSheetBinding.inflate(inflater, container, false)
        initCallback()
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isHideable = false
        }

        binding.layoutCollapsed.visibility = View.GONE
        binding.layoutExpanded.visibility = View.VISIBLE
    }

    private fun initCallback() {
        binding.btnSendBack.setOnClickListener {
            lifecycleScope.launch {
                viewModel.excursionPended(id)
            }
        }

        binding.btnReject.setOnClickListener {
            lifecycleScope.launch {
                viewModel.excursionRejected(id)
            }
        }
    }

    companion object {
        private const val ID = "ID"

        fun newInstance(id: Long): DisapproveExcursionFragment {
            val fragment = DisapproveExcursionFragment()
            fragment.arguments = Bundle().apply {
                putLong(ID, id)
            }
            return fragment
        }
    }
}
