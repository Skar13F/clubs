package com.example.clubs.model

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clubs.R

class StudentAdapter(private val studentList: List<Student>) :
    RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.bind(student)

        // Alternar colores de fondo para mejor legibilidad
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.table_row_even)
            )
        } else {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.table_row_odd)
            )
        }
    }

    override fun getItemCount() = studentList.size

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvId: TextView = view.findViewById(R.id.tvStudentId)
        private val tvName: TextView = view.findViewById(R.id.tvStudentName)
        private val tvEmail: TextView = view.findViewById(R.id.tvStudentEmail)

        fun bind(student: Student) {
            tvId.text = student.id
            tvName.text = student.name
            tvEmail.text = student.email

            // Destacar texto si es necesario
            if (student.id.startsWith("admin")) {
                tvId.setTextColor(Color.RED)
            }
        }
    }
}