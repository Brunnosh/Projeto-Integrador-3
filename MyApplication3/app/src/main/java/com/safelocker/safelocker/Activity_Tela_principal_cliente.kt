package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.safelocker.Activity_MainActivity
import com.safelocker.R
import com.safelocker.databinding.ActivityTelaPrincipalClienteBinding

private lateinit var appBarConfiguration: AppBarConfiguration
private lateinit var binding: ActivityTelaPrincipalClienteBinding

class Activity_Tela_principal_cliente : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        binding = ActivityTelaPrincipalClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração da barra de ferramentas
        setSupportActionBar(binding.appBarTelaPrincipalCliente.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_tela_principal_cliente)

        // Configuração da navegação com base nos destinos do Navigation Component
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_maps, R.id.nav_card, R.id.nav_compra
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Recupera o usuário atualmente autenticado
        val user = FirebaseAuth.getInstance().currentUser
        var nomedoc : String? = null
        if (user != null) {
            val emailuser = user.email

            // Recupera o nome do usuário do Firestore com base no email
            db.collection("pessoa")
                .whereEqualTo("email", user.email)
                .get()
                .addOnSuccessListener { usuarios ->
                    usuarios.forEach(){usuario ->
                        nomedoc = usuario.getString("nome")
                    }

                    // Define o email e o nome do usuário nos campos de cabeçalho do NavigationView
                    val headerView = navView.getHeaderView(0)
                    val emailTextView = headerView.findViewById<TextView>(R.id.textoEmail)
                    val nomeTextView = headerView.findViewById<TextView>(R.id.textoNome)
                    emailTextView.text = emailuser
                    nomeTextView.text = nomedoc
                }
        }
    }

    // Infla o menu da barra de ferramentas
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tela_principal_cliente, menu)
        return true
    }

    // Configura o comportamento dos itens do menu da barra de ferramentas
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_out ->{
                // Efetua o logout do usuário e retorna à tela de login
                Firebase.auth.signOut()
                startActivity(Intent(this, Activity_MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Configura o comportamento do botão de voltar da barra de ferramentas
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_tela_principal_cliente)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
