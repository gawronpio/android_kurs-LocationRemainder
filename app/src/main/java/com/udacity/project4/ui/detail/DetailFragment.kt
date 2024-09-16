package com.udacity.project4.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val args: DetailFragmentArgs by navArgs()
        viewModel.setvariables(args.reminderData)

        binding.detailViewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailRadiusSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.radius.value = progress.toFloat()
                binding.detailRadiusText.text = String.format(getString(R.string.unit_m), progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.detailRadiusSeekbar.progress = viewModel.radius.value?.toInt() ?: getString(R.string.default_radius).toInt()

        binding.detailSaveBtn.setOnClickListener {
            if(viewModel.title.value.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.fields_needed), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Bundle().apply { putParcelable("newReminder", viewModel.getPoi()) }
            parentFragmentManager.setFragmentResult("reminderData", result)
            findNavController().popBackStack(R.id.remindersListFragment, false)
        }
    }
}