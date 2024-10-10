package com.safelocker.safelocker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.safelocker.R
import com.safelocker.databinding.ActivityArmarioAbertoMomentaneamenteBinding

class Activity_Armario_Aberto_Momentaneamente : AppCompatActivity() {

   private lateinit var binding: ActivityArmarioAbertoMomentaneamenteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityArmarioAbertoMomentaneamenteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this,Activity_Tela_Principal_Gerente::class.java))
            this.finish()
        }
    }
}
