package com.example.locationremainder.ui.main

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationremainder.R
import com.example.locationremainder.data.PoiDao
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val poiDao: PoiDao by lazy { PoiDatabase.getInstance(requireContext()).poiDatabaseDao }

    private val viewModel: MainViewModel by viewModels() {
        MainViewModelFactory(poiDao, requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val manager = LinearLayoutManager(activity)
        binding.poiRecycler.layoutManager = manager
        val adapter = MainRecyclerAdapter(MainListener { poiId ->
            // TODO implement navigate do detail fragment
        })
        binding.poiRecycler.adapter = adapter

        return binding.root
    }
}