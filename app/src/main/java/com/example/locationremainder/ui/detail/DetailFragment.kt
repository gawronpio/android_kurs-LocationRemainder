package com.example.locationremainder.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.locationremainder.R
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private lateinit var viewModel: DetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val poiDao = PoiDatabase.getInstance(requireContext()).poiDatabaseDao
        viewModel = ViewModelProvider(this, DetailViewModelFactory(poiDao, requireActivity().application))[DetailViewModel::class.java]

        val args: DetailFragmentArgs by navArgs()
        viewModel.setvariables(args.poiData)

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
            if(binding.detailTitle.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.fields_needed), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Bundle().apply { putParcelable("newPoi", viewModel.getPoi()) }
            parentFragmentManager.setFragmentResult("poiData", result)
            findNavController().popBackStack(R.id.mainFragment, false)
        }
    }
}