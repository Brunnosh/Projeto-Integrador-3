package com.safelocker.safelocker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelocker.databinding.ActivityCartaoTelaBinding

// QUANDO SALVAR AS INFORMAÇÕES DO CARTÃO, DAR UMA MENSAGEM POR SNACKBAR QUE SALVOU
// ( CASO DEU ERRADO, MENSAGEM DE ERRO ) E IR ALTOMÂTICAMENTE PARA TELA PRINCIPAL DO USUÁRIO !

private lateinit var binding: ActivityCartaoTelaBinding
private lateinit var auth: FirebaseAuth

class Activity_Tela_Cartao : AppCompatActivity() {
    // Instância do Firestore para acessar o banco de dados Firestore
    private val db = FirebaseFirestore.getInstance()

    private val calendario = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Obtém a instância de autenticação do Firebase
        auth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        binding = ActivityCartaoTelaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Define a orientação da atividade como retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        // Configura o clique do botão de voltar para retornar à tela principal do usuário
        binding.icVoltar.setOnClickListener {
            val intent = Intent(this, Activity_Tela_principal_cliente::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            this.finish()
        }

        // Configura o clique do botão para inserir os detalhes do cartão
        binding.btnInserirCartao.setOnClickListener {
            val nome = binding.EtNome.text.toString()
            val num = binding.EtNumCartao.text.toString()
            val validadeMes = binding.EtValidadeMes.text.toString()
            val validadeAno = binding.EtValidadeAno.text.toString()
            val codigo = binding.EtCodSeguranca.text.toString()

            when {
                nome.isEmpty() -> {
                    mensagem(it, "Preencha o nome")
                }
                !nome.matches("[a-zA-ZÀ-ú ]+".toRegex()) -> {
                    mensagem(it, "O nome deve conter apenas letras")
                }
                num.isEmpty() -> {
                    mensagem(it, "Prencha o Número do cartão")
                }
                !verificardata(validadeMes, validadeAno, it) -> {

                }
                codigo.isEmpty() -> {
                    mensagem(it, "Prencha o código de segurança")
                }
                !verificarnum(num) -> {
                    mensagem(it, "Número muito curto")
                }
                else -> {
                    val user = auth.currentUser
                    val validade = "$validadeMes/$validadeAno"



                    // Verifica se o usuário está logado
                    if (user != null) {
                        val uid = user.uid
                        // Consulta o Firestore para verificar se o cartão já existe
                        db.collection("cartoes")
                            .whereEqualTo("UserID", uid)
                            .get()
                            .addOnSuccessListener { documents ->
                                var cartaojaexiste = false
                                documents.forEach() { document ->
                                    val numdoc = document.getString("num")
                                    val validadedoc = document.getString("validade")
                                    if (numdoc == num && validadedoc == validade) {
                                        cartaojaexiste = true
                                        return@forEach
                                    }
                                }
                                if (cartaojaexiste) {
                                    mensagem(it, "Cartão já existe")
                                } else {
                                    // Salva os detalhes do cartão no Firestore
                                    salvarDadosNoFirestore(nome, num, validade, uid)
                                    // Retorna à tela principal do usuário
                                    val intent = Intent(this, Activity_Tela_principal_cliente::class.java)
//                                    intent.flags =
//                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    startActivity(intent)
                                    this.finish()
                                }
                            }
                    } else {
                        mensagem(it, "Faça login antes de inserir")
                    }
                }
            }
        }
    }

    // Método para exibir uma mensagem por Snackbar
    private fun mensagem(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    // Método para salvar os detalhes do cartão no Firestore
    private fun salvarDadosNoFirestore(nome: String, num: String, validade: String, uid: String) {
        val cartaoMap = hashMapOf(
            "nome" to nome,
            "num" to num,
            "validade" to validade,
            "UserID" to uid
        )

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("cartoes").document()
                .set(cartaoMap)
                .addOnSuccessListener {
                    // Exibe uma mensagem de sucesso ao salvar os detalhes do cartão
                   mensagemPositive(binding.root,"Cartão Salvo")


                }
                .addOnFailureListener { e ->
                    // Exibe uma mensagem de erro se houver um problema ao salvar os dados
                    mensagem(binding.root,"Erro ao obter o documento para o ID de usuário")
                }
        }
    }

    private fun verificardata(validademes: String,validadeano: String, view: View) : Boolean{
        var valido = false

        if (validademes.length != 2){
            mensagem(view, "Mês de validade do cartão não é válido!")
            return valido
        }

        if (validadeano.length != 2){
            mensagem(view, "Ano deve ter apenas dois dígitos!")
            return valido
        }

        // Data de hoje

        val mesatual = calendario.get(Calendar.MONTH) + 1  // ano + 1 pq janeiro = 0 nessa funcao
        val anoatualstring = calendario.get(Calendar.YEAR).toString()

        val anoatualchar1 = anoatualstring[2]
        val anoatualchar2 = anoatualstring[3]

        val anoatual = ("$anoatualchar1$anoatualchar2").toInt()


        when {
            validademes.toInt() > 12 -> {
                mensagem(view, "Mês inválido")
                return valido
            }
            validadeano.toInt() < anoatual -> {
                mensagem(view, "Cartão vencido")
                return valido
            }
            validadeano.toInt() == anoatual && validademes.toInt() < mesatual -> {
                mensagem(view, "Cartão vencido")
                return valido
            }

            else -> {
                valido = true
                return valido
            }

        }
    }

    private fun verificarnum(num : String) : Boolean{
        var valido = false

        if(num.length < 16){
            return valido
        } else {
            valido = true
            return valido
        }
    }
}


//@ feito por cicera
private fun mensagemPositive(view: View, mensagem: String) {
    val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT)
    snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
    snackbar.setTextColor(Color.parseColor("#FFFFFF"))
    snackbar.show()
}

