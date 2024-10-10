package com.safelocker.safelocker

import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.databinding.ActivityMinhasLocacoesBinding

private lateinit var binding: ActivityMinhasLocacoesBinding


class Activity_MinhasLocacoes : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() // Referência ao Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinhasLocacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        // Obter todas as locações
        val locacoesRef = db.collection("locacao")
        val listaLocacoes = mutableListOf<DataClass_Locacoes>()
        locacoesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id
                    val armarioId = document.getString("armarioId")
                    val status = document.getString("status")
                    val tempo = document.getString("tempo")
                    val userId = document.getString("userId")

                    // Verificar se todos os dados necessários foram obtidos
                    if (armarioId != null && status != null && tempo != null && userId != null) {

                        db.collection("unidades").document(armarioId)
                            .get()
                            .addOnSuccessListener { armarioDocument ->
                                val nomeLugar = armarioDocument.getString("referencia")
                                val codigo_armario = armarioDocument.getString("codigo_armario")
                                val rua = armarioDocument.getString("rua")
                                val numero = armarioDocument.getDouble("numero")

                                // Verificar se todos os dados necessários foram obtidos
                                if (nomeLugar != null && codigo_armario != null && rua != null && numero != null) {
                                    // Se sim, criar um objeto DataClass_Locacoes com os dados obtidos
                                    val locacao = DataClass_Locacoes(id, armarioId, status, tempo, userId, nomeLugar, codigo_armario, rua, numero)

                                    // Adicionar a locação à lista
                                    listaLocacoes.add(locacao)

                                    // Atualizar o RecyclerView quando todos os dados forem coletados
                                    val adapter = Adapter_Locacoes(this,listaLocacoes)
                                    binding.recyclerView.layoutManager = LinearLayoutManager(this)
                                    binding.recyclerView.adapter = adapter

                                }
                            }

                    }

                }
            }
            .addOnFailureListener { exception ->
                // Falha ao obter documentos
                Log.w(ContentValues.TAG, "Erro ao obter documentos: ", exception)
            }
    }
}
