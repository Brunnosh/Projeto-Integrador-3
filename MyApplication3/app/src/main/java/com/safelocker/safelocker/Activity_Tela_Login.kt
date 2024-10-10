package com.safelocker

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.safelocker.databinding.ActivityLoginTelaBinding
import com.safelocker.safelocker.Activity_Tela_Principal_Gerente
import com.safelocker.safelocker.Activity_Tela_principal_cliente

private lateinit var binding: ActivityLoginTelaBinding
private lateinit var auth: FirebaseAuth
private lateinit var progressDialog: Dialog


class Activity_Tela_Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLoginTelaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        InicializarProgressBar()
        VoltarParaTelaRegistro()
        VaiParaTelaEsqueceuSenha()
        ConfiguraçãoDoBotãoEntrar()

        binding.icVoltar.setOnClickListener {
            startActivity(Intent(this,Activity_MainActivity::class.java))
            this.finish()
        }

    }
    // Inicializando o Dialog progressBar
    private fun InicializarProgressBar(){

        progressDialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
        progressDialog.setContentView(view)
        progressDialog.setCancelable(false)
        hideProgressDialog()
    }

    // Configura o clique no botão de login
    private fun ConfiguraçãoDoBotãoEntrar(){
        binding.btnEntrar.setOnClickListener {
            ValidarCamposEmaileSenha()
        }
    }
    // Valida se os campos de email e senha estão preenchidos
    private fun ValidarCamposEmaileSenha(){

        val email = binding.EtEmail.text.toString()
        val password = binding.EtSenha.text.toString()
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            when {
                email.isEmpty() -> {
                    mensagem(binding.root, "Preencha seu email")
                }
                password.isEmpty() -> {
                    mensagem(binding.root, "Preencha sua senha")
                }
                password.length <= 5 -> {
                    mensagem(binding.root, "A senha precisa ter pelo menos seis caracteres")
                }
            }
        } else {
            AutenticaçãodeUsuario()
    }


    }
    // Autenticação do usuário utilizando o Firebase Authentication
    private fun AutenticaçãodeUsuario(){
        val email = binding.EtEmail.text.toString()
        val password = binding.EtSenha.text.toString()
        // Autenticação do usuário utilizando o Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Se o login for bem-sucedido, vai para a tela principal do cliente
                    showProgressDialog()
                    Log.d(ContentValues.TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                    val userId = user?.uid

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




                } else {
                    // Se o login falhar, exibe uma mensagem de erro
                    Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Email ou Senha inválidos. Tente novamente ",
                        LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }



    // Exibe uma mensagem utilizando Snackbar
    private fun mensagem(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    // Método para atualizar a interface do usuário
    private fun updateUI(user: FirebaseUser?) {
    }

    // Configura o clique no botão "criarConta2 e criarConta"para ir para a tela de registro
    private fun VoltarParaTelaRegistro(){
        binding.criarConta2.setOnClickListener{
            startActivity(Intent(this,Activity_Tela_Registro::class.java))
            this.finish()
        }

        binding.criarConta.setOnClickListener{
            startActivity(Intent(this,Activity_Tela_Registro::class.java))
            this.finish()
        }
    }
    private fun VaiParaTelaEsqueceuSenha(){
        // Configura o clique no texto "Esqueceu sua senha?"
        binding.TVEsqueceusenha2.setOnClickListener {
            val esqueceu_senha = Intent(this, Activity_EsqueceuSenha::class.java)
            startActivity(esqueceu_senha)
        }
    }


    private fun showProgressDialog() {
        progressDialog.show()
    }
    private fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}
