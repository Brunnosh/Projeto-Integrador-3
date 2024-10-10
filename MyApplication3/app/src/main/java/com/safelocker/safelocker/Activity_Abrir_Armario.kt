package com.safelocker.safelocker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.safelocker.databinding.ActivityAbrirArmarioBinding

class Activity_Abrir_Armario : AppCompatActivity() {

    private lateinit var binding: ActivityAbrirArmarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbrirArmarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConfiguraBotaoAbrirArmario()
        ConfiguraBotaoEncerrarLocacao()

    }
    private fun ConfiguraBotaoAbrirArmario(){
        // Obtém o ID da locação passado através da intent
        // Configura o botão para abrir o armário momentaneamente
        binding.ConstaintAbrir.setOnClickListener {
            startActivity(Intent(this,Activity_Armario_Aberto_Momentaneamente::class.java))
            this.finish()
        }
    }
    private fun ConfiguraBotaoEncerrarLocacao(){
        // Obtém o ID da locação passado através da intent
        val locacaoId = intent.getStringExtra("LocacaoId")
        // Configura o botão para encerrar a locação e limpar a NFC
        binding.btnEncerrar.setOnClickListener {
            startActivity(Intent(this,Activity_NFC_ClearActivity::class.java).putExtra("LocacaoId", locacaoId))
            this.finish()
        }
    }


}
