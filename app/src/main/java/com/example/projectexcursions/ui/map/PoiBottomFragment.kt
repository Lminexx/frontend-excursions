package com.example.projectexcursions.ui.map

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.projectexcursions.R
import com.example.projectexcursions.databinding.PlacesBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import androidx.core.view.isGone

private const val COLLAPSED_HEIGHT = 228

@AndroidEntryPoint
class PoiBottomFragment : BottomSheetDialogFragment() {

    private lateinit var animation: Animation
    private lateinit var binding: PlacesBottomSheetBinding
    private var poiName: String? = null
    private var poiDesc: String? = null
    private var poiAddress: String? = null
    private val viewModel: MapViewModel by activityViewModels()
    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlacesBottomSheetBinding.inflate(inflater, container, false)
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.appear_pop_up)

        initCallback()
        subscribe()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val density = requireContext().resources.displayMetrics.density

        dialog?.let {
            val bottomSheet = binding.parent
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.peekHeight = (COLLAPSED_HEIGHT * density).toInt()
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

            poiName = arguments?.getString("poiName")
            poiAddress = arguments?.getString("poiAddress")
            poiDesc = arguments?.getString("poiDesc")

            Log.d("endPoint", "${viewModel.endPoint.value?.latitude}, ${viewModel.endPoint.value?.longitude}")
            Log.d("curPoint", "${viewModel.curPoint.value?.latitude}, ${viewModel.curPoint.value?.longitude}")

            Log.d("Args:", "name: $poiName\n" +
                    "address: $poiAddress\n" +
                    "desc: $poiDesc")

            binding.poiName.text = poiName
            binding.poiAddressExpanded.text = poiAddress
            binding.poiDescExpanded.text = poiDesc

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    with(binding) {
                        if (slideOffset > 0) {
                            layoutCollapsed.alpha = 1 - 2 * slideOffset
                            layoutExpanded.alpha = slideOffset * slideOffset

                            if (slideOffset > 0.5) {
                                layoutCollapsed.visibility = View.GONE
                                layoutExpanded.visibility = View.VISIBLE
                            }

                            if (slideOffset < 0.5 && binding.layoutExpanded.isVisible) {
                                layoutCollapsed.visibility = View.VISIBLE
                                layoutExpanded.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            })
        }
    }

    private fun initCallback() {
        binding.makePath.setOnClickListener {
            lifecycleScope.launch {
                Log.d("routeFinished", viewModel.routeFinished.value.toString())
                viewModel.getRoute()
            }
        }

        binding.closePath.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setMessage("Завершить текущий маршрут?")
                .setPositiveButton("Да") { dialog, _ ->
                    dialog.dismiss()
                    viewModel.endRoute()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }
    }

    fun subscribe() {
        viewModel.routeFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                if (binding.makePath.isGone) {
                    binding.closePath.visibility = View.GONE
                    binding.makePath.visibility = View.VISIBLE
                    binding.makePath.startAnimation(animation)
                }
            } else {
                if (binding.closePath.isGone) {
                    binding.makePath.visibility = View.GONE
                    binding.closePath.visibility = View.VISIBLE
                    binding.closePath.startAnimation(animation)
                }
            }
        }
    }

    companion object {
        fun newInstance(name: String, address: String, description: String, isMainMap: Boolean): PoiBottomFragment {
            val fragment = PoiBottomFragment()
            val args = Bundle()
            args.putString("poiName", name)
            args.putString("poiAddress", address)
            args.putString("poiDesc", description)
            args.putBoolean("main_map", isMainMap)
            fragment.arguments = args
            return fragment
        }
    }
}