package com.safelocker.safelocker.ui.locacao

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.databinding.FragmentLocacaoBinding
import com.safelocker.safelocker.DataClass_Locacoes
import com.safelocker.safelocker.Adapter_Locacoes

class locacaoFragment : Fragment() {

    private var _binding: FragmentLocacaoBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocacaoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listaLocacoes = mutableListOf<DataClass_Locacoes>()
        val adapter = Adapter_Locacoes(requireContext(), listaLocacoes)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val locacoesRef = db.collection("locacao")
        locacoesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val armarioId = document.getString("armarioId")
                    val status = document.getString("status")
                    val tempo = document.getString("tempo")
                    val userId = document.getString("userId")

                    if (armarioId != null && status != null && tempo != null && userId != null) {
                        val user = auth.currentUser
                        if (user != null) {
                            val uid = user.uid
                            if (userId != null && userId == uid) {
                                db.collection("unidades").document(armarioId)
                                    .get()
                                    .addOnSuccessListener { armarioDocument ->
                                        val nomeLugar = armarioDocument.getString("referencia")
                                        val codigoArmario = armarioDocument.getString("codigo_armario")
                                        val rua = armarioDocument.getString("rua")
                                        val numero = armarioDocument.getDouble("numero")

                                        if (nomeLugar != null && codigoArmario != null && rua != null && numero != null) {
                                            val locacao = DataClass_Locacoes(document.id, armarioId, status, tempo, userId, nomeLugar, codigoArmario, rua, numero)
                                            listaLocacoes.add(locacao)

                                            // Ordena a lista com os itens com status "pendente" primeiro
                                            val listaLocacoesOrdenada = listaLocacoes.sortedByDescending { it.status == "pendente" }
                                            adapter.updateList(listaLocacoesOrdenada)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("locacaoFragment", "Error getting document", exception)
                                    }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("locacaoFragment", "Error getting documents: ", exception)
                Toast.makeText(
                    requireContext(),
                    "Error getting documents: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
