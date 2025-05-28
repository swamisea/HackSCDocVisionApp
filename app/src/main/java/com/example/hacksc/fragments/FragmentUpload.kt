package com.example.hacksc.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hacksc.R
import com.example.hacksc.databinding.FragmentUploadBinding
import java.io.File


class FragmentUpload : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        binding.btnUpload.setOnClickListener {
            selectPdfFile()
        }


        return binding.root
    }

    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                savePdfLocally(uri)
            }
        }
    }

    private fun savePdfLocally(pdfUri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(pdfUri)
        val file = File(requireContext().filesDir, "selectedDocument.pdf")

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Toast.makeText(requireContext(), "PDF saved successfully!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.upload_to_speech_input)
    }


}