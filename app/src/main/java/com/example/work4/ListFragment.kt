package com.example.work4

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.work4.databinding.FragmentListBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewCreated()
    }

    private fun recyclerViewCreated() {
        val recyclerView: RecyclerView = binding.recyclerView

        // Путь к файлу data.txt
        val file = File(requireContext().getExternalFilesDir(null), "photos/date.txt")

        // Проверка, существует ли файл
        if (file.exists()) {
            try {
                // Чтение строк из файла
                val date = file.readLines()
                    .filter { it.startsWith("Фото сделано: ") } // Фильтрация строк, которые начинаются с "Фото сделано"
                    .map { it.replace("Фото сделано: ", "").trim() } // Удаление префикса "Фото сделано: "

                // Инициализация адаптера с данными
                val adapter = MyAdapter(date)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())

            } catch (e: Exception) {
                e.printStackTrace()
                // Вывод ошибки в случае проблемы с файлом
                println("Ошибка при чтении файла: ${e.message}")
            }
        } else {
            println("Файл date.txt не найден")
        }
    }
}
