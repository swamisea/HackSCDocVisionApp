package com.example.hacksc.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hacksc.databinding.FragmentSpeechInputBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.File
import java.lang.Long
import java.nio.Buffer
import java.util.Locale

class FragmentSpeechInput : Fragment() {

    private lateinit var binding:FragmentSpeechInputBinding
    private lateinit var textToSpeech: TextToSpeech
    //val pdfFile = File(requireContext().filesDir, "selectedDocument.pdf")
    private var recognizedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpeechInputBinding.inflate(layoutInflater)

        binding.speechInput.setOnClickListener {
            binding.edittextview.isFocusable = false
            binding.btnAskQuery.isEnabled = false
            binding.textview.text = null
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something")
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    Long.valueOf(1000000)
                )
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(200000))
                result.launch(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        binding.edittextview.isFocusableInTouchMode = true
        binding.btnAskQuery.isEnabled = true
        binding.btnAskQuery.setOnClickListener {
            recognizedText = ""
            recognizedText = binding.edittextview.text.toString()
            Log.d("TAG","HELLO THERE: $recognizedText")
            val text = sendPostRequestWithPdfAndString(recognizedText,"localhost:8000/api/chat")
            //val text = "Hello there Rithvik how are you doing today?"
            if (text != null) {
                textToSpeech(text)
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 200) {
            if ((grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                // Permission denied, handle accordingly
                Toast.makeText(requireContext(), "Permission denied for audio recording", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result->
                if(result.resultCode == Activity.RESULT_OK){
                    val results = result.data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                    ) as ArrayList<String>
                    recognizedText = ""
                    recognizedText = results[0]
                    Log.d("TAG","HELLO THERE: $recognizedText")
                    val text = sendPostRequestWithPdfAndString(recognizedText,"localhost:8000/api/chat")
                    //val text = "Hello there Rithvik how are you doing today?"
                    if (text != null) {
                        textToSpeech(text)
                    }
                }
    }

    private fun textToSpeech(text: String){
        textToSpeech = TextToSpeech(activity, TextToSpeech.OnInitListener {
            if(it == TextToSpeech.SUCCESS){
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(1.0f)
                textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null)
            }
        })
    }

    fun sendPostRequestWithPdfAndString( text: String, url: String): String? {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val textMediaType = "text/plain".toMediaTypeOrNull()
                val textRequestBody = text.toRequestBody(textMediaType)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("input_text", text) // Adds the string
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseText = response.body?.string()
                    } else {
                        // Handle the error, possibly log or show a message
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception
            }
        }
        return "Hello"
    }

}