package br.com.contarim.cuidaidoso

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import java.text.SimpleDateFormat
import java.util.*

// --- TELAS DE AUTH (MANTIDAS) ---

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Image(painterResource(id = R.drawable.idoso), null, Modifier.size(180.dp).clip(CircleShape))
        Spacer(Modifier.height(24.dp))
        Text("CuidaIdoso", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Button(onContinue, Modifier.fillMaxWidth().height(56.dp).padding(top = 20.dp)) { Text("INICIAR") }
    }
}

@Composable
fun AuthScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center) {
        Text("CuidaIdoso", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
        Button(onLoginClick, Modifier.fillMaxWidth().height(56.dp)) { Text("ENTRAR") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onRegisterClick, Modifier.fillMaxWidth().height(56.dp)) { Text("CADASTRAR") }
    }
}

@Composable
fun LoginScreen(usuarios: List<Usuario>, onBack: () -> Unit, onLoginSuccess: (Usuario) -> Unit) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center) {
        Text("Login", style = MaterialTheme.typography.headlineLarge)
        if (erro) Text("E-mail ou senha incorretos", color = Color.Red)
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(senha, { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button({
            val user = usuarios.find { it.email == email && it.senha == senha }
            if (user != null) onLoginSuccess(user) else erro = true
        }, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("ENTRAR") }
        TextButton(onBack) { Text("Voltar") }
    }
}

@Composable
fun RegistroScreen(onBack: () -> Unit, onRegisterSuccess: (Usuario) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Novo Cadastro", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(senha, { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button({ if(email.isNotEmpty() && senha.isNotEmpty()) onRegisterSuccess(Usuario(nome, email, "0", senha)) }, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("CADASTRAR") }
        TextButton(onBack) { Text("Voltar") }
    }
}

// --- TELAS DO APP PRINCIPAL ---

@Composable
fun ListaMedicamentosScreen(lista: MutableList<Medicamento>, historico: MutableList<HistoricoEvento>) {
    var showDialog by remember { mutableStateOf(false) }
    var nomeMed by remember { mutableStateOf("") }
    var doseMed by remember { mutableStateOf("") }

    // Estado para a hora com suporte a controle de cursor
    var horaFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Medicamentos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button({ showDialog = true }, Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Icon(Icons.Default.Add, null)
            Text(" ADICIONAR REMÉDIO")
        }
        LazyColumn {
            items(lista) { med ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        headlineContent = { Text(med.nome, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${med.dosagem} às ${med.horario}") },
                        leadingContent = { Icon(Icons.Default.Alarm, null, tint = Color(0xFF1976D2)) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Novo Medicamento") },
            confirmButton = {
                Button({
                    if (nomeMed.isNotEmpty() && horaFieldValue.text.length >= 5) {
                        lista.add(Medicamento(nomeMed, horaFieldValue.text, doseMed))
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        historico.add(0, HistoricoEvento("Adicionado: $nomeMed", sdf.format(Date()), Icons.Default.AddCircle))
                        nomeMed = ""; horaFieldValue = TextFieldValue(""); doseMed = ""; showDialog = false
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ showDialog = false }) { Text("Cancelar") } },
            text = {
                Column {
                    OutlinedTextField(nomeMed, { nomeMed = it }, label = { Text("Nome") })

                    // CORREÇÃO: Campo de Hora com Cursor Inteligente
                    OutlinedTextField(
                        value = horaFieldValue,
                        onValueChange = { input ->
                            val digits = input.text.filter { it.isDigit() }
                            if (digits.length <= 4) {
                                val formatted = if (digits.length >= 3) {
                                    digits.substring(0, 2) + ":" + digits.substring(2)
                                } else digits

                                // Mantém o cursor sempre no final após a formatação
                                horaFieldValue = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            }
                        },
                        label = { Text("Hora (Ex: 0800)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(doseMed, { doseMed = it }, label = { Text("Dose") })
                }
            }
        )
    }
}

@Composable
fun ContatosEmergenciaScreen(lista: MutableList<ContatoEmergencia>) {
    var showDialog by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Contatos de Emergência", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button({ showDialog = true }, Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
            Icon(Icons.Default.Add, null)
            Text(" ADICIONAR CONTATO")
        }
        LazyColumn {
            items(lista) { contato ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    ListItem(
                        headlineContent = { Text(contato.nome, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${contato.parentesco} - ${contato.telefone}") },
                        leadingContent = { Icon(Icons.Default.ContactPhone, null, tint = Color.Red) },
                        trailingContent = { IconButton({ lista.remove(contato) }) { Icon(Icons.Default.Delete, null) } }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Novo Contato") },
            confirmButton = {
                Button({
                    if (nome.isNotEmpty() && tel.isNotEmpty()) {
                        lista.add(ContatoEmergencia(nome, tel, parentesco))
                        nome = ""; tel = ""; parentesco = ""; showDialog = false
                    }
                }) { Text("Adicionar") }
            },
            text = {
                Column {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome Completo") })
                    OutlinedTextField(tel, { tel = it }, label = { Text("Telefone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    OutlinedTextField(parentesco, { parentesco = it }, label = { Text("Parentesco (Ex: Filho)") })
                }
            }
        )
    }
}

@Composable
fun PerfilUsuarioScreen(usuario: Usuario, onLogout: () -> Unit) {
    var nome by remember { mutableStateOf(usuario.nome) }
    var email by remember { mutableStateOf(usuario.email) }
    var senha by remember { mutableStateOf(usuario.senha) }
    var editando by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp), tint = Color.Gray)
        Text("Gerenciar Conta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, enabled = editando, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, enabled = editando, modifier = Modifier.fillMaxWidth())

        if (editando) {
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Nova Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        if (!editando) {
            Button({ editando = true }, Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, null)
                Text(" EDITAR INFORMAÇÕES")
            }
        } else {
            Button({
                usuario.nome = nome
                usuario.email = email
                usuario.senha = senha
                editando = false
            }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("SALVAR ALTERAÇÕES")
            }
            TextButton({ editando = false }) { Text("Cancelar") }
        }

        Spacer(Modifier.height(16.dp))

        Divider()

        Spacer(Modifier.height(16.dp))

        Button(onLogout, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("SAIR DA CONTA")
        }
    }
}

@Composable
fun HistoricoTimelineScreen(historico: List<HistoricoEvento>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Histórico Recente", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
        items(historico) { ev ->
            ListItem(
                headlineContent = { Text(ev.titulo) },
                supportingContent = { Text(ev.dataHora) },
                leadingContent = { Icon(ev.icone, null, tint = Color(0xFF1976D2)) }
            )
            Divider()
        }
    }
}