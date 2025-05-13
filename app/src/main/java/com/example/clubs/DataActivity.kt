package com.example.clubs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clubs.model.Student
import com.example.clubs.model.StudentAdapter
import com.google.firebase.firestore.FirebaseFirestore

class DataActivity : AppCompatActivity() {

    private lateinit var etStudentId: EditText
    private lateinit var btnVerify: Button
    private lateinit var btnBack: Button
    private lateinit var spinnerClubs: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter

    private lateinit var db: FirebaseFirestore
    private var clubList: MutableList<String> = mutableListOf()
    private var studentList: MutableList<Student> = mutableListOf()
    private val clubNameToIdMap: MutableMap<String, String> = mutableMapOf()
    private var isAdmin: Boolean = false
    private var selectedClubId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        etStudentId = findViewById(R.id.etStudentId)
        btnVerify = findViewById(R.id.btnVerify)
        spinnerClubs = findViewById(R.id.spinnerClubs)
        recyclerView = findViewById(R.id.recyclerView)

        db = FirebaseFirestore.getInstance()

        adapter = StudentAdapter(studentList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnVerify.setOnClickListener {
            val studentId = etStudentId.text.toString().trim()

            if (studentId.isNotEmpty()) {
                verifyUser(studentId)
            } else {
                Toast.makeText(this, "Por favor ingresa un ID", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Esto debería funcionar si navegaste correctamente

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun verifyUser(studentId: String) {
        db.collection("admins").document(studentId)
            .get()
            .addOnSuccessListener { adminDoc ->
                if (adminDoc.exists()) {
                    // Es admin
                    isAdmin = true
                    Toast.makeText(this, "Bienvenido Admin", Toast.LENGTH_SHORT).show()
                    loadClubsForAdmin()
                } else {
                    // Es estudiante
                    db.collection("students").document(studentId)
                        .get()
                        .addOnSuccessListener { studentDoc ->
                            if (studentDoc.exists()) {
                                isAdmin = false
                                Toast.makeText(this, "Estudiante encontrado", Toast.LENGTH_SHORT).show()

                                val clubsMap = studentDoc.get("clubs") as? Map<String, Boolean>
                                val clubId = clubsMap?.entries?.find { it.value }?.key

                                if (clubId != null) {
                                    selectedClubId = clubId
                                    // Buscar el nombre del club desde la colección
                                    db.collection("clubs").document(clubId)
                                        .get()
                                        .addOnSuccessListener { clubDoc ->
                                            val clubName = clubDoc.getString("name") ?: "Club no encontrado"
                                            showClubInSpinner(clubName)
                                            loadStudentData(studentId)
                                        }
                                } else {
                                    Toast.makeText(this, "No estás inscrito en ningún club", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "ID no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }

    private fun showClubInSpinner(clubName: String) {
        clubList.clear()
        clubList.add(clubName)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clubList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClubs.adapter = adapter
        spinnerClubs.visibility = View.VISIBLE
        spinnerClubs.isEnabled = false
    }

    private fun loadClubsForAdmin() {
        db.collection("clubs")
            .get()
            .addOnSuccessListener { result ->
                clubList.clear()
                clubNameToIdMap.clear()
                clubList.add("Selecciona un club")

                for (document in result) {
                    val name = document.getString("name") ?: continue
                    val id = document.id
                    clubList.add(name)
                    clubNameToIdMap[name] = id
                }

                val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, clubList)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClubs.adapter = adapterSpinner
                spinnerClubs.visibility = View.VISIBLE

                spinnerClubs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedClubName = clubList[position]
                        if (selectedClubName != "Selecciona un club") {
                            val realClubId = clubNameToIdMap[selectedClubName]
                            if (realClubId != null) {
                                selectedClubId = realClubId
                                loadClubData(realClubId)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun loadStudentData(studentId: String) {
        db.collection("students").document(studentId)
            .get()
            .addOnSuccessListener { studentDoc ->
                val name = studentDoc.getString("name") ?: ""
                val email = studentDoc.getString("email") ?: ""
                val student = Student(studentId, name, email)
                studentList.clear()
                studentList.add(student)
                adapter.notifyDataSetChanged()
            }
    }

    private fun loadClubData(clubId: String) {
        db.collection("clubs").document(clubId)
            .get()
            .addOnSuccessListener { clubDoc ->
                val members = clubDoc.get("members") as? Map<*, *> ?: return@addOnSuccessListener
                val students = mutableListOf<Student>()
                val memberIds = members.filterValues { it == true }.keys.mapNotNull { it as? String }

                if (memberIds.isEmpty()) {
                    Toast.makeText(this, "No hay estudiantes en este club", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var loadedCount = 0
                for (id in memberIds) {
                    db.collection("students").document(id)
                        .get()
                        .addOnSuccessListener { studentDoc ->
                            val name = studentDoc.getString("name") ?: ""
                            val email = studentDoc.getString("email") ?: ""
                            students.add(Student(id, name, email))

                            loadedCount++
                            if (loadedCount == memberIds.size) {
                                // Cuando ya se cargaron todos
                                studentList.clear()
                                studentList.addAll(students)
                                val adapter = StudentAdapter(studentList)
                                recyclerView.layoutManager = LinearLayoutManager(this)
                                recyclerView.adapter = adapter
                            }
                        }
                        .addOnFailureListener {
                            loadedCount++
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener los datos del club", Toast.LENGTH_SHORT).show()
            }
    }
}
