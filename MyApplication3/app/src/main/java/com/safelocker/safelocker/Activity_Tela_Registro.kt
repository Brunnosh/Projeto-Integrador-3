package com.safelocker

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.databinding.ActivityRegistroTelaBinding
import com.safelocker.safelocker.Activity_Tela_Principal_Gerente
import com.safelocker.safelocker.Activity_Tela_principal_cliente
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private lateinit var binding: ActivityRegistroTelaBinding
private lateinit var auth: FirebaseAuth
private lateinit var progressDialog: Dialog


class Activity_Tela_Registro : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityRegistroTelaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Inicializando o Dialog progressBar
        progressDialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
        progressDialog.setContentView(view)
        progressDialog.setCancelable(false)
        hideProgressDialog()



        // Configuração do clique do texto "Já tem uma conta?" para redirecionar para a tela de login
        binding.TvJatemUmaConta.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Login::class.java))
            this.finish()
        }

        // Configuração do clique do botão "Já tem uma conta?" para redirecionar para a tela de login
        binding.TvJatemUmaContaBtn.setOnClickListener {
            startActivity(Intent(this, Activity_Tela_Login::class.java))
        }

        // Configuração do clique do ícone de voltar para retornar à tela principal
        binding.icVoltar.setOnClickListener {
            val intent = Intent(this, Activity_MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            this.finish()
        }

        // Configuração do toque no campo de data de nascimento para exibir o seletor de data
        binding.EtNascimento.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showDatePickerDialog()
            }
            return@setOnTouchListener true
        }

        // Inicialização do objeto FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configuração do clique do botão de efetuar cadastro
        binding.btnEfetuarCadastro.setOnClickListener {
            val email = binding.EtEmail.text.toString()
            val senha = binding.EtSenha.text.toString()
            val confirmarSenha = binding.EtConfirmarSenha.text.toString()
            val nome = binding.EtNome.text.toString()
            val cpf = binding.EtCPF.text.toString()
            Log.i("INFOTEST","onCreate: $cpf")
            val data = binding.EtNascimento.text.toString()
            val telefone = binding.EtTelefone.text.toString()
            val gerente : Boolean = false


            // Verificação dos campos obrigatórios e das regras de validação
            when {
                nome.isEmpty() -> {
                    mensagemNegativa(it, "Preencha seu nome")
                }
                !nome.matches("[a-zA-ZÀ-ú ]+".toRegex()) -> {
                    mensagemNegativa(it, "O nome deve conter apenas letras")
                }
                cpf.isEmpty() -> {
                    mensagemNegativa(it, "Preencha seu CPF")
                }
                cpf.length != 14 -> {
                    mensagemNegativa(it, "O CPF precisa ter 11 caracteres")
                }
                data.isEmpty() -> {
                    mensagemNegativa(it, "Preencha sua data de nascimento")
                }
                !validarIdade(data) -> {
                    mensagemNegativa(it, "A idade deve estar entre 14 e 120 anos")
                }
                telefone.isEmpty() -> {
                    mensagemNegativa(it, "Preencha seu telefone")
                }
                telefone.length != 15 -> {
                    mensagemNegativa(it, "O telefone precisa ter 11 caracteres")
                }
                email.isEmpty() -> {
                    mensagemNegativa(it, "Preencha seu email")
                }
                senha.isEmpty() -> {
                    mensagemNegativa(it, "Preencha sua senha")
                }
                senha.length < 6 -> {
                    mensagemNegativa(it, "A senha precisa ter pelo menos seis caracteres")
                }
                senha != confirmarSenha -> {
                    mensagemNegativa(it, "As senhas não coincidem")
                }
                else -> {
                    // Verifica se o email já está em uso
                    db.collection("pessoa")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { pessoas ->
                            var contaJaExiste = false

                            pessoas.forEach { pessoa ->
                                if (pessoa.getString("email") == email) {
                                    contaJaExiste = true
                                    return@forEach
                                }
                            }

                            if (contaJaExiste) {
                                mensagemNegativa(it, "O email fornecido já está em uso")
                            } else {
                                // Criação da conta no Firebase Auth
                                auth.createUserWithEmailAndPassword(email, senha)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Envio de email de verificação
                                            val user = auth.currentUser
                                            user?.sendEmailVerification()
                                            // Salva os dados no Firestore
                                            salvarDadosNoFirestore(nome, cpf, data, telefone, email,gerente)
                                            showProgressDialog()
                                            mensagemPositive(binding.root,"Conta criada. Verifique seu email para confirmar")

                                            auth.signInWithEmailAndPassword(email, senha)
                                                .addOnCompleteListener(this) { task ->
                                                    if (task.isSuccessful) {
                                                        // Se o login for bem-sucedido, vai para a tela principal do cliente
                                                        Log.d(ContentValues.TAG, "signInWithEmail:success")

                                                        val user = auth.currentUser
                                                        updateUI(user)
                                                        val userId = user?.uid

                                                        val userDocRef = FirebaseFirestore.getInstance().collection("pessoa").document(userId!!)
                                                        userDocRef.get().addOnSuccessListener { documentSnapshot ->
                                                            if (documentSnapshot.exists()) {
                                                                val gerente = documentSnapshot.getBoolean("gerente")
                                                                Log.i("INFOTEST","---->>: $gerente")
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
                                                        mensagemNegativa(binding.root,"Falha na autenticação, tente novamente")
                                                        updateUI(null)
                                                    }
                                                    this.finish()
                                                }

                                        }
                                    }
                            }
                        }
                }
            }
        }
    }

    // Método para exibir o seletor de data
    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val ano = c.get(Calendar.YEAR)
        val mes = c.get(Calendar.MONTH)
        val dia = c.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(this,android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            { datePicker, i, i2, i3 ->
                val SelectDate:Calendar = c
                SelectDate.set(Calendar.YEAR,i)
                SelectDate.set(Calendar.MONTH,i2)
                SelectDate.set(Calendar.DAY_OF_MONTH,i3)
                val date = formatDate.format(SelectDate.time)
                binding.EtNascimento.text = Editable.Factory.getInstance().newEditable(date)},ano,mes,dia)
        datePickerDialog.show()
    }

    // Método para exibir uma mensagem por Snackbar
    private fun mensagemNegativa(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun mensagemPositive(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    // Método para salvar os dados do usuário no Firestore
    private fun salvarDadosNoFirestore(nome: String, cpf: String, data: String, telefone: String, email: String,gerente:Boolean) {
        val pessoaMap = hashMapOf(
            "nome" to nome,
            "cpf" to cpf,
            "data_nascimento" to data,
            "telefone" to telefone,
            "email" to email,
            "gerente" to gerente

        )

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("pessoa").document(it.uid)
                .set(pessoaMap)
                .addOnSuccessListener {
                  mensagemPositive(binding.root,"Bem Vindo")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        baseContext,
                        "Erro ao enviar dados: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    private fun updateUI(user: FirebaseUser?) {
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

// Função para validar a idade com base na data de nascimento
private fun validarIdade(dataNascimento: String): Boolean {
    val partesData = dataNascimento.split("/")
    val anoNascimento = partesData[2].toInt()
    val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
    val idade = anoAtual - anoNascimento

    return idade in 14..120
}