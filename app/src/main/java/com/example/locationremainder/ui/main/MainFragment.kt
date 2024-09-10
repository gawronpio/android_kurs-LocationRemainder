package com.example.locationremainder.ui.main

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationremainder.R
import com.example.locationremainder.data.PoiDao
import com.example.locationremainder.data.PoiDatabase
import com.example.locationremainder.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val poiDao: PoiDao by lazy { PoiDatabase.getInstance(requireContext()).poiDatabaseDao }

    private val viewModel: MainViewModel by viewModels() {
        MainViewModelFactory(poiDao, requireActivity().application)
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.logout_menu_item -> {
                    AuthUI.getInstance()
                        .signOut(requireContext())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                findNavController().popBackStack(R.id.welcomeFragment, false)
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.logout), Toast.LENGTH_SHORT).show()
                            }
                        }
                    return true
                }
            }
            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

        val manager = LinearLayoutManager(activity)
        binding.poiRecycler.layoutManager = manager
        val adapter = MainRecyclerAdapter(MainListener { poiId ->
            // TODO implement navigate do detail fragment
        })
        binding.poiRecycler.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(Firebase.auth.currentUser != null) {
                    requireActivity().finishAffinity()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }
}