package com.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.safelocker.databinding.ActivityMainBinding
import com.safelocker.safelocker.Activity_MapsActivity
import com.safelocker.safelocker.Activity_Tela_Principal_Gerente
import com.safelocker.safelocker.Activity_Tela_principal_cliente

// Variáveis globais para referenciar a instância de FirebaseAuth e a vinculação do layout da atividade
private lateinit var auth: FirebaseAuth
private lateinit var binding: ActivityMainBinding

class Activity_MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataClassSwipeList: ArrayList<DataClass_swipe>
    private lateinit var Adapter_swipe: Adapter_swipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Inicializa a instância de FirebaseAuth
        auth = Firebase.auth

        // Inicializa a interface do usuário
        init()

        // Configura os eventos de clique nos botões "Entrar" e "Mapa"
        eventoClickBtnEntrar()
        eventoClickBtnMapa()
    }

    // Método para lidar com o evento de clique no botão "Entrar"
    private fun eventoClickBtnEntrar() {
        // Verifica o estado de autenticação ao iniciar a atividade
        onStart()

        // Configura o evento de clique no botão "Entrar"
        binding.BtnEntrar.setOnClickListener {
            // Inicia a atividade de bifurcação de usuário (login ou registro)
            startActivity(Intent(this, Activity_Tela_Login::class.java))
            // Finaliza a atividade atual para liberar recursos de memória
            destroiActivity()
        }
    }

    // Método para lidar com o evento de clique no botão "Mapa"
    private fun eventoClickBtnMapa() {
        // Configura o evento de clique no botão "Mapa"
        binding.BtnMapa.setOnClickListener {
            // Inicia a atividade do mapa
            startActivity(Intent(this, Activity_MapsActivity::class.java))
            // Finaliza a atividade atual para liberar recursos de memória
            destroiActivity()
        }
    }


    // Método para inicializar a interface do usuário
    private fun init() {
        recyclerView = binding.lista
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()  // "ajuda para centralizar elemnetos da lista na tela"
        snapHelper.attachToRecyclerView(recyclerView)
        dataClassSwipeList = ArrayList()
        addDatatoList()
        Adapter_swipe = Adapter_swipe(dataClassSwipeList)
        recyclerView.adapter = Adapter_swipe
    }

    // Método para adicionar dados à lista de DataClass_swipe
    private fun addDatatoList() {
        dataClassSwipeList.add(
            DataClass_swipe(
                "Sinta-se seguro em todos os momentos",
                R.drawable.primeira_img_recycleview, "SafeLocker", "",
                R.drawable.backgrounddefault, ""
            )
        )
        dataClassSwipeList.add(
            DataClass_swipe(
                "Encontre armários mais próximos do seu ",
                R.drawable.segunda_img_recycleview, "SafeLocker", "Rolê",
                R.drawable.backgroundwhite, ""
            )
        )
        dataClassSwipeList.add(
            DataClass_swipe(
                "Coloque seus pertences no",
                R.drawable.terceira_img_recycleview, "SafeLocker", "",
                R.drawable.backgrounddefault, "Armário"
            )
        )
        dataClassSwipeList.add(
            DataClass_swipe(
                "Recupere as coisas ao seu tempo com ",
                R.drawable.quarta_img_reycleview, "SafeLocker", "NFC",
                R.drawable.backgroundwhite, ""
            )
        )
    }

    // Método para finalizar a atividade atual
    private fun destroiActivity() {
        this.finish()
    }

    // Método onStart para verificar o estado de autenticação ao iniciar a atividade
    public override fun onStart() {
        super.onStart()
        // Verifica se há um usuário atualmente autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser?.uid

            val userDocRef = FirebaseFirestore.getInstance().collection("pessoa").document(userId!!)
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val gerente = documentSnapshot.getBoolean("gerente")
                    if (gerente == true) {
                        startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
                    } else {
                        startActivity(Intent(this, Activity_Tela_principal_cliente::class.java))
                    }
                }
            }
        }
    }
}

/*  usar a troca de cor do background com uma imagemview que tera a cor determinada para cada screen*/
