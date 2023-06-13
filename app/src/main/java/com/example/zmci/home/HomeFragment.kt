package com.example.zmci.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zmci.R
import com.example.zmci.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detection.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_CameraFragment)
        }
        preferences.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_SettingsFragment)
        }
        reports.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_DetectionFragment)
        }
        about.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_aboutFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}