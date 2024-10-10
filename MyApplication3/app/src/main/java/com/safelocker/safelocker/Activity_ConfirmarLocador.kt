package com.safelocker.safelocker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.safelocker.R
import com.safelocker.databinding.ActivityConfirmarLocadorBinding

class Activity_ConfirmarLocador : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmarLocadorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityConfirmarLocadorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val photoImage = binding.image

        // Obtém o ID da locação e a URL da foto passados através da intent
        val locacaoId = intent.getStringExtra("LocacaoId")
        val photoUrl = intent.getStringExtra("PhotoUrl")

        // Carrega a imagem usando Glide, se a URL não for nula ou vazia
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .into(photoImage)
        } else {
            // Tratar caso de URL nula ou vazia
            Toast.makeText(this, "URL da imagem é inválida", Toast.LENGTH_SHORT).show()
        }

        // Configura o botão para prosseguir e abrir o armário
        binding.btnProsseguir.setOnClickListener {
            startActivity(Intent(this, Activity_Abrir_Armario::class.java).putExtra("LocacaoId", locacaoId))
            this.finish()
        }

        // Configura o ícone de voltar para retornar à tela principal do gerente
        binding.icVoltar.setOnClickListener {
            startActivity(Intent(this,Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }
    }
}
