package br.com.contarim.cuidaidoso

import androidx.compose.ui.graphics.vector.ImageVector

data class Medicamento(
    val nome: String,
    val horario: String,
    val dosagem: String,
    val isRecorrente: Boolean = false // Novo campo
)
data class Usuario(var nome: String, var email: String, var idade: String, var senha: String)
data class ContatoEmergencia(val nome: String, val telefone: String, val parentesco: String)
data class HistoricoEvento(val titulo: String, val dataHora: String, val icone: ImageVector)
data class Consulta(val tipo: String, val especialidade: String, val data: String, val hora: String)