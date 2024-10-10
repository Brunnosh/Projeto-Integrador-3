package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.safelocker.Activity_MainActivity
import com.safelocker.R
import com.safelocker.databinding.ActivityTelaPrincipalGerenteBinding

private lateinit var binding: ActivityTelaPrincipalGerenteBinding

class Activity_Tela_Principal_Gerente : AppCompatActivity() {
    // Declaração da instância de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        enableEdgeToEdge()

        binding = ActivityTelaPrincipalGerenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização da instância de FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Verifica se há um usuário atualmente autenticado e mostra seu email
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val emailuser = user.email
            val emailTextView = binding.textoEmail
            emailTextView.text = emailuser
        }

        // Configura o botão "Liberar Locação" para iniciar a QrCodeScannerActivity
        binding.btnLiberarLocacao.setOnClickListener {
            startActivity(Intent(this,Activity_QrCodeScanner::class.java))
        }

        // Configura o ícone de logout para deslogar o usuário e retornar à Activity_MainActivity
        binding.logoutIcon.setOnClickListener {
            auth.signOut() // Faz logout do usuário atual
            startActivity(Intent(this, Activity_MainActivity::class.java)) // Inicia Activity_MainActivity
            finish() // Finaliza a atividade atual (Activity_Tela_Principal_Gerente)
        }

        // Configura o botão "Abrir Armário" para iniciar a Activity_NFC_ReaderActivity
        binding.btnAbrirArmario.setOnClickListener {
            startActivity(Intent(this,Activity_NFC_ReaderActivity::class.java))
        }
    }
}
