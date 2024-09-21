package com.example.work4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.work4.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация binding
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        // Инициализация камеры и других компонентов
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = binding.Camera
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверка разрешений
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Обработка нажатия кнопки для съемки и сохранения времени
        binding.captureBtn.setOnClickListener {
            takePhotoAndSaveTimestamp()
        }

        // Кнопка для перехода на другой фрагмент
        binding.onListFragmentBtn.setOnClickListener {
            findNavController().navigate(R.id.action_cameraFragment_to_listFragment)
        }
    }

    // Метод для съемки фотографии и записи времени
    private fun takePhotoAndSaveTimestamp() {
        val imageCapture = imageCapture ?: return

        // Создание имени файла для фотографии
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(requireContext().getExternalFilesDir(null), "$name.jpg")

        // Опции для съемки
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Съемка фотографии
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Когда фотография сделана, сохраняем дату и время
                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date())
                    saveTimestampToFile(currentTime)
                }
            }
        )
    }

    // Метод для сохранения даты и времени в файл data.txt
    private fun saveTimestampToFile(timestamp: String) {
        // Создание папки в доступном месте (внутренняя память)
        val folder = File(requireContext().getExternalFilesDir(null), "photos")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // Создание или открытие файла data.txt
        val file = File(folder, "date.txt")
        if (!file.exists()) {
            file.createNewFile()
        }

        // Записываем время и дату в файл
        file.appendText("Фото сделано: $timestamp\n")
        println("Время фото успешно сохранено: $timestamp")
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Создание экземпляра ImageCapture для съемки фотографий
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
