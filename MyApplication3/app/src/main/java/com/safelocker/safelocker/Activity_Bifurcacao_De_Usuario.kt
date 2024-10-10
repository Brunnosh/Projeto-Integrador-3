package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.safelocker.Activity_MainActivity
import com.safelocker.Activity_Tela_Login
import com.safelocker.Activity_Tela_Registro
import com.safelocker.databinding.ActivityBifurcacaoDeUsuarioBinding

// Esta atividade permite que os usuários escolham entre fazer login ou registrar uma nova conta
private lateinit var binding: ActivityBifurcacaoDeUsuarioBinding

class Activity_Bifurcacao_De_Usuario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBifurcacaoDeUsuarioBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        // Configuração do clique do botão "Login" para redirecionar para a tela de login
        eventoClickBtnLogin()

        // Configuração do clique do botão "Registrar" para redirecionar para a tela de registro
        eventoClickBtnRegistrar()

        // Configuração do clique do ícone de voltar para retornar à tela principal
        voltaTelaParaAnterior()
    }

    // Método para configurar o clique do botão "Login"
    private fun eventoClickBtnLogin() {
        binding.btnLoin.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Login::class.java))
            destroiActivity()
        }
    }

    // Método para configurar o clique do botão "Registrar"
    private fun eventoClickBtnRegistrar() {
        binding.btnRegistrar.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Registro::class.java))
            destroiActivity()
        }
    }

    // Método para finalizar a atividade atual
    private fun destroiActivity() {
        this.finish()
    }

    // Método para voltar à tela anterior (Activity_MainActivity) ao clicar no ícone de voltar
    private fun voltaTelaParaAnterior() {
        binding.icVoltar.setOnClickListener {
            startActivity(Intent(this, Activity_MainActivity::class.java))
            destroiActivity()
        }
    }
}
