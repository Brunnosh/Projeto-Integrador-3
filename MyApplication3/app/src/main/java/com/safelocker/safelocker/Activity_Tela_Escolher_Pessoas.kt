package com.safelocker.safelocker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.safelocker.databinding.ActivityTelaEscolherPessoasBinding

private lateinit var binding: ActivityTelaEscolherPessoasBinding

class Activity_Tela_Escolher_Pessoas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaEscolherPessoasBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding = ActivityTelaEscolherPessoasBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Obtém o ID da locação passado pela intent
        val locacaoId = intent.getStringExtra("LocacaoId")

        // Configura o botão de voltar para redirecionar para a tela principal do gerente
        binding.icVoltar.setOnClickListener{
            startActivity(Intent(this, Activity_Tela_Principal_Gerente::class.java))
        }

        // Configura os checkboxes para permitir apenas uma seleção
        binding.checkBox1.setOnClickListener {
            binding.checkBox1.isChecked = true
            binding.checkBox2.isChecked = false
        }

        binding.checkBox2.setOnClickListener {
            binding.checkBox1.isChecked = false
            binding.checkBox2.isChecked = true
        }

        // Configura o botão "Continuar" para iniciar a CameraActivity com base na seleção do usuário
        binding.btnContinuar.setOnClickListener {
            if(binding.checkBox1.isChecked || binding.checkBox2.isChecked){
                var contador = 0
                if(binding.checkBox1.isChecked) {
                    contador = 1
                }else{
                    contador = 2
                }
                startActivity(Intent(this, Activity_Camera::class.java).putExtra("contador",contador).putExtra("LocacaoId",locacaoId))
            }else {
                // Se nenhuma opção foi selecionada, exibe uma mensagem de Snackbar
                mensagem(it, "Selecione uma opção")
            }
        }
    }

    // Função para exibir uma mensagem de Snackbar
    private fun mensagem(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A")) // Define a cor de fundo da Snackbar
        snackbar.setTextColor(Color.parseColor("#FFFFFF")) // Define a cor do texto da Snackbar
        snackbar.show()
    }
}
