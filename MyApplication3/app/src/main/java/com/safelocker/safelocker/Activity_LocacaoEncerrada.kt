package com.safelocker.safelocker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.R
import com.safelocker.databinding.ActivityLocacaoEncerradaBinding
import java.util.Calendar
import java.util.concurrent.TimeUnit

class Activity_LocacaoEncerrada : AppCompatActivity() {

    private lateinit var binding: ActivityLocacaoEncerradaBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityLocacaoEncerradaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        val locacaoId = intent.getStringExtra("LocacaoId")
        updateStatusInFirestore(locacaoId)

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }
    }

    private fun updateStatusInFirestore(locacaoId: String?) {
        locacaoId?.let {
            val docRef = firestore.collection("locacao").document(it)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val startTime = document.getTimestamp("inicio")
                        val armarioId = document.getString("armarioId")

                        // Obtém a hora atual
                        val currentTime = Timestamp.now()

                        // Verifica se o horário atual é antes das 18:00
                        val calendar = Calendar.getInstance()
                        calendar.time = currentTime.toDate()
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

                        if (hourOfDay < 18) {
                            startTime?.let { start ->
                                // Calcula a diferença de tempo em minutos
                                val diffInMillis = currentTime.toDate().time - start.toDate().time
                                val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

                                if (armarioId != null) {
                                    val unidadesRef = firestore.collection("unidades").document(armarioId)
                                    unidadesRef.get()
                                        .addOnSuccessListener { unidadeDoc ->
                                            if (unidadeDoc != null) {
                                                val precoTotalString = unidadeDoc.getString("preco")
                                                precoTotalString?.let { priceString ->
                                                    // Remove "R$ " e a vírgula para converter para double
                                                    val precoTotal = priceString.replace("R$ ", "").replace(",", ".").toDoubleOrNull() ?: 0.0

                                                    // Calcula o preço por hora
                                                    val precoPorHora = precoTotal / 10.0

                                                    // Calcula o preço do aluguel até o horário atual, arredondando para a próxima hora cheia
                                                    val horasArredondadas = Math.ceil(diffInMinutes / 60.0).toInt()

                                                    // Verifica se o tempo de locação é menor que uma hora
                                                    val horasDeLocacao = if (horasArredondadas == 0) 1 else horasArredondadas

                                                    // Calcula o preço do aluguel com base no tempo de locação arredondado
                                                    val precoAluguel = horasDeLocacao * precoPorHora

                                                    // Calcula o valor da caução
                                                    val caucao = precoTotal - precoAluguel


                                                    val caucaoFormatted = "R$ ${"%.2f".format(caucao)}"

                                                    // Salva o preço da caução no documento de locação
                                                    docRef.update("caucao", caucaoFormatted)
                                                        .addOnSuccessListener {
                                                        }
                                                        .addOnFailureListener { e ->
                                                            e.printStackTrace()
                                                        }

                                                    // Atualiza o binding.tvCaucao com o valor da caução formatado
                                                    binding.tvCaucao.text = "O cliente recebeu uma caução de $caucaoFormatted reais"
                                                    Log.i("INFOTESTE","getHour: $hourOfDay")
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            e.printStackTrace()
                                        }
                                }
                            }
                        } else {
                            // Se o horário atual for após as 18:00, não calcula a caução
                            docRef.update("caucao", "R$ 0.00")
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                    e.printStackTrace()
                                }

                            binding.tvCaucao.text = "O cliente não recebeu caução pois já passou das 18:00"
                        }

                        // Atualizando o status para "finalizado"
                        docRef.update("status", "finalizado")
                            .addOnSuccessListener {
                                // Verifica na coleção armarios e atualiza o status
                                updateArmarioStatus(locacaoId)
                            }
                            .addOnFailureListener { e ->
                                e.printStackTrace()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }



    private fun updateArmarioStatus(locacaoId: String) {
        val armariosRef = firestore.collection("armarios")
        armariosRef.whereEqualTo("locacaoId", locacaoId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val armarioRef = armariosRef.document(document.id)
                    val updates = hashMapOf<String, Any>(
                        "status" to "disponível",
                        "locacaoId" to FieldValue.delete()
                    )
                    armarioRef.update(updates)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
