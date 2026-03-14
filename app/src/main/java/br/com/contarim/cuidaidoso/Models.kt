package br.com.contarim.cuidaidoso

import androidx.compose.ui.graphics.vector.ImageVector

data class Medicamento(
    val nome: String,
    val horario: String,
    val dosagem: String
)

data class Usuario(
    var nome: String,
    var email: String,
    var idade: String,
    var senha: String
)

data class ContatoEmergencia(
    val nome: String,
    val telefone: String,
    val parentesco: String
)

data class HistoricoEvento(
    val titulo: String,
    val dataHora: String,
    val icone: ImageVector
)