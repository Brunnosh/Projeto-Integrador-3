package com.safelocker.safelocker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.R
import com.safelocker.databinding.ActivityLocacaoConcluidaBinding

class Activity_LocacaoConcluida : AppCompatActivity() {

    private lateinit var binding: ActivityLocacaoConcluidaBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        binding = ActivityLocacaoConcluidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o Firestore
        firestore = FirebaseFirestore.getInstance()

        // Obtém o ID da locação da intent
        val locacaoId = intent.getStringExtra("LocacaoId")

        // Atualiza o status da locação no Firestore e reserva o armário
        updateStatusInFirestore(locacaoId)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura o botão de voltar para retornar à tela principal do gerente
        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }
    }

    // Atualiza o status da locação no Firestore e reserva o armário correspondente
    private fun updateStatusInFirestore(locacaoId: String?) {
        locacaoId?.let {
            val docRef = firestore.collection("locacao").document(it)
            val updates = hashMapOf<String, Any>(
                "status" to "em andamento",
                "inicio" to FieldValue.serverTimestamp()
            )
            docRef.update(updates)
                .addOnSuccessListener {
                    // Após atualizar o status da locação, verifica e reserva o armário
                    checkAndReserveArmario(locacaoId)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    // Verifica e reserva o armário correspondente à locação
    private fun checkAndReserveArmario(locacaoId: String) {
        val locacaoRef = firestore.collection("locacao").document(locacaoId)

        locacaoRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val armarioId = document.getString("armarioId")
                    if (armarioId != null) {
                        val armariosRef = firestore.collection("armarios")
                        armariosRef.whereEqualTo("armarioId", armarioId).whereEqualTo("status", "disponível").get()
                            .addOnSuccessListener { documents ->
                                if (documents != null && !documents.isEmpty) {

                                    val document = documents.documents[0]
                                    val armarioRef = armariosRef.document(document.id)
                                    val updates = hashMapOf<String, Any>(
                                        "status" to "ocupado",
                                        "locacaoId" to locacaoId
                                    )
                                    armarioRef.update(updates)
                                        .addOnSuccessListener {
                                            // Atualiza a UI com o número do armário reservado
                                            val numeroArmario = document.get("numero")
                                            binding.tvArmario.text = "Número do armário: $numeroArmario"
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Activity_LocacaoConcluida", "Erro ao atualizar o status do armário", e)
                                            Toast.makeText(this, "Erro ao atualizar o status do armário", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Activity_LocacaoConcluida", "Erro ao buscar armários", exception)
                                Toast.makeText(this, "Erro ao buscar armários", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "ID de armário não encontrado no documento de locacao", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Activity_LocacaoConcluida", "Documento de locação não encontrado")
                    Toast.makeText(this, "Documento de locação não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Activity_LocacaoConcluida", "Erro ao obter documento de locação", exception)
                Toast.makeText(this, "Erro ao obter documento de locação", Toast.LENGTH_SHORT).show()
            }
    }
}
