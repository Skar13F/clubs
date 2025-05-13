package com.example.clubs

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var spinnerClubs: Spinner
    private lateinit var formLayout: LinearLayout
    private lateinit var etStudentId: EditText
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button

    private lateinit var db: FirebaseFirestore
    private var clubList: MutableList<String> = mutableListOf()
    private var selectedClubId: String? = null

    private val clubIdMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        spinnerClubs = findViewById(R.id.spinnerClubs)
        formLayout = findViewById(R.id.formLayout)
        etStudentId = findViewById(R.id.etStudentId)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)

        db = FirebaseFirestore.getInstance()

        cargarClubsDesdeFirestore()

        btnSave.setOnClickListener {
            guardarEstudiante()
        }
    }

    private fun cargarClubsDesdeFirestore() {
        db.collection("clubs")
            .get()
            .addOnSuccessListener { result ->
                clubList.clear()
                clubIdMap.clear()

                clubList.add("Selecciona un club")
                for (document in result) {
                    val name = document.getString("name") ?: continue
                    val id = document.id
                    clubList.add(name)
                    clubIdMap[name] = id
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clubList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClubs.adapter = adapter

                spinnerClubs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selected = clubList[position]
                        if (selected != "Selecciona un club") {
                            formLayout.visibility = View.VISIBLE
                            selectedClubId = clubIdMap[selected]
                        } else {
                            formLayout.visibility = View.GONE
                            selectedClubId = null
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar clubes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarEstudiante() {
        val id = etStudentId.text.toString().trim()
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || selectedClubId == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("clubs")
            .get()
            .addOnSuccessListener { result ->

                val clubsMap = mutableMapOf<String, Boolean>()
                val updates = mutableListOf<Pair<String, Map<String, Any>>>()

                for (document in result) {
                    val clubId = document.id
                    val isSelected = clubId == selectedClubId

                    // Crear un map para el campo 'clubs' del estudiante
                    clubsMap[clubId] = isSelected

                    // Actualizar el estatus de la relacion club → members.{id} = true/false
                    val update = mapOf("members.$id" to isSelected)
                    updates.add(Pair(clubId, update))
                }

                // Actualizar datos estudiante (crear o reemplazar)
                val studentData = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "email" to email,
                    "clubs" to clubsMap
                )

                db.collection("students").document(id)
                    .set(studentData)
                    .addOnSuccessListener {

                        // Actualizar los regiistros de los clubs
                        var clubsUpdated = 0
                        for ((clubId, updateMap) in updates) {
                            db.collection("clubs").document(clubId)
                                .update(updateMap)
                                .addOnSuccessListener {
                                    clubsUpdated++
                                    if (clubsUpdated == updates.size) {
                                        Toast.makeText(this, "Estudiante registrado correctamente", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al actualizar el club $clubId", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al registrar estudiante", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar clubes", Toast.LENGTH_SHORT).show()
            }
    }
}
