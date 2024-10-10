package com.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.databinding.ActivityEsqueceuSenhaBinding

class Activity_EsqueceuSenha : AppCompatActivity() {
    private lateinit var binding: ActivityEsqueceuSenhaBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsqueceuSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        // Configura o evento de clique no ícone de voltar
        binding.icVoltar.setOnClickListener{
            // Volta para a tela de login
            val voltar = Intent(this, Activity_Tela_Login::class.java)
            startActivity(voltar)
            // Finaliza a atividade atual (Activity_EsqueceuSenha)
            this.finish()
        }

        // Configura o evento de clique no botão "Entrar na Conta"
        binding.entrarConta2.setOnClickListener{
            // Inicia a tela de login
            val login = Intent(this, Activity_Tela_Login::class.java)
            startActivity(login)
        }

        // Inicializa o Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Configura o evento de clique no botão "Enviar"
        binding.btnEnviar.setOnClickListener{
            // Obtém o email digitado pelo usuário
            val email = binding.etEmail.text.toString()
            if (email.isEmpty()) {
                // Se o campo de email estiver vazio, exibe uma mensagem
                mensagem(it, "Preencha seu email")
            } else {
                // Consulta a coleção "pessoa" no Firestore para verificar se o email existe
                db.collection("pessoa")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { pessoas ->
                        var contaExiste = false

                        pessoas.forEach() { pessoa ->
                            val emaildb = pessoa.getString("email")

                            if (emaildb == email) {
                                contaExiste = true
                                return@forEach
                            }
                        }

                        // Se a conta existe, envia um email de redefinição de senha
                        if (contaExiste) {
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Email de recuperação enviado para seu email", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Se a conta não existe, exibe uma mensagem
                            mensagem(it, "Não existe nenhuma conta com esse e-mail")
                        }
                    }
            }
        }
    }

    // Método para exibir uma mensagem usando Snackbar
    private fun mensagem(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }
}
