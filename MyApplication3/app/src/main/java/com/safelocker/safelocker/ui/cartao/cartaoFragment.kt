package com.safelocker.safelocker.ui.cartao

import Adapter_Card
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.R
import com.safelocker.databinding.FragmentCartaoBinding
import com.safelocker.safelocker.Model_Card
import com.safelocker.safelocker.Activity_Tela_Cartao



class cartaoFragment : Fragment(), Adapter_Card.OnItemClickListener {

    // Inicializa a instância do FirebaseAuth
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Obtém o usuário atual
    private val user = auth.currentUser
    // Inicializa a instância do FirebaseFirestore
    private val db = FirebaseFirestore.getInstance()

    // Variável de ligação para o layout do fragmento
    private var _binding: FragmentCartaoBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapterdeletecartao : Adapter_Card

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa o ViewModel relacionado a este fragmento
        val cartaoViewModel = ViewModelProvider(this).get(cartaoViewModel::class.java)

        // Infla o layout do fragmento
        _binding = FragmentCartaoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Obtém a referência para o TextView e o RecyclerView do layout
        val textView: TextView = binding.appCompatTextView
        val recyclerViewCartao = binding.recyclerViewCartao
        recyclerViewCartao.layoutManager = LinearLayoutManager(requireContext())



                // Define o listener para o botão de adicionar cartão
        binding.btnTelaAddCartao.setOnClickListener(){
            startActivity(Intent(requireContext(), Activity_Tela_Cartao::class.java))
        }

        // Verifica se há um usuário autenticado
        if (user != null) {

            // Consulta os documentos na coleção "cartoes" que correspondem ao UserID atual
            db.collection("cartoes")
                .whereEqualTo("UserID", user.uid)
                .get()
                .addOnSuccessListener { cartoes ->
                    // Lista para armazenar os modelos de cartão
                    val cardlist = mutableListOf<Model_Card>()

                    // Itera sobre os documentos retornados
                    cartoes.forEach(){cartao ->

                        // Obtém os campos do cartão (nome, número, validade)
                        val cartaonome = cartao.getString("nome")
                        val cartaonumero = cartao.getString("num")
                        val cartaovalidade = cartao.getString("validade")
//                        val cartaoimagemchipe = cartao.getString("")

                        // Verifica se os campos não são nulos
                        if (cartaonome != null && cartaonumero != null && cartaovalidade != null) {

                            // Cria um modelo de cartão e adiciona à lista
                            val card = Model_Card(cartaonome, cartaonumero, cartaovalidade,R.drawable.chipe)
                            cardlist.add(card)
                        }
                    }

                    // Define o adaptador do RecyclerView com a lista de cartões
                    recyclerViewCartao.adapter = Adapter_Card(cardlist, this)
                    adapterdeletecartao = Adapter_Card(cardlist, this)
                }
        }
        return root
    }

    // Limpa a referência ao layout do fragmento no momento em que ele é destruído
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onButtonClick(card: Model_Card) {
        Toast.makeText(requireContext(), "CARTAO REMOVIDO : ${card.numcartao}", Toast.LENGTH_LONG).show()

        if(user != null){
            db.collection("cartoes")
                .whereEqualTo("UserID", user.uid)
                .whereEqualTo("num", card.numcartao)
                .get()
                .addOnSuccessListener { cartoes ->
                    for (cartao in cartoes){
                        db.collection("cartoes").document(cartao.id).delete()
                    }

                    adapterdeletecartao.notifyDataSetChanged()
                }


        }

    }

    fun reiniciar(){

    }

}
