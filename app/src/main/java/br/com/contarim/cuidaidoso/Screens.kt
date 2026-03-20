package br.com.contarim.cuidaidoso

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import java.text.SimpleDateFormat
import java.util.*

// --- AUXILIARES ---
fun obterDataHoraAtual(): String {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun LogoApp(tamanho: Dp = 150.dp) {
    Image(
        painter = painterResource(id = R.drawable.idoso),
        contentDescription = "Logo",
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    )
}

// --- TELAS DE ACESSO ---

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoApp(200.dp)
        Spacer(Modifier.height(32.dp))
        Text("CuidaIdoso", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Sua família em boas mãos.", color = Color.Gray)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("INICIAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun AuthScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoApp(130.dp)
        Spacer(Modifier.height(40.dp))
        Text("Bem-vindo!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onLoginClick, modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("ENTRAR") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRegisterClick, modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("CADASTRAR") }
    }
}

@Composable
fun LoginScreen(usuarios: List<Usuario>, onBack: () -> Unit, onLoginSuccess: (Usuario) -> Unit) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoApp(100.dp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(senha, { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        if (erro) Text("Dados incorretos", color = Color.Red)
        Button({
            val user = usuarios.find { it.email.trim().lowercase() == email.trim().lowercase() && it.senha == senha }
            if (user != null) onLoginSuccess(user) else erro = true
        }, Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("ENTRAR") }
        TextButton(onBack) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Voltar")
            }
        }
    }
}

@Composable
fun RegistroScreen(onBack: () -> Unit, onRegisterSuccess: (Usuario) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoApp(100.dp)
        Spacer(Modifier.height(24.dp))
        Text("Crie sua Conta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(senha, { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Button({ if(email.isNotBlank() && senha.isNotBlank()) onRegisterSuccess(Usuario(nome, email, "0", senha)) }, Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("CADASTRAR") }
        TextButton(onBack) { Text("Voltar") }
    }
}

// --- TELAS PRINCIPAIS ---

@Composable
fun ListaMedicamentosScreen(lista: MutableList<Medicamento>, historico: MutableList<HistoricoEvento>) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var editandoMed by remember { mutableStateOf<Medicamento?>(null) }

    var nomeMed by remember { mutableStateOf("") }
    var doseMed by remember { mutableStateOf("") }
    var horaFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isRecorrente by remember { mutableStateOf(false) }

    // Reset ou preenchimento de campos ao abrir diálogo
    LaunchedEffect(showDialog) {
        if (showDialog) {
            nomeMed = editandoMed?.nome ?: ""
            doseMed = editandoMed?.dosagem ?: ""
            val h = editandoMed?.horario ?: ""
            horaFieldValue = TextFieldValue(h, TextRange(h.length))
            isRecorrente = editandoMed?.isRecorrente ?: false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Medicamentos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), Arrangement.spacedBy(8.dp)) {
            Button({
                editandoMed = null
                showDialog = true
            }, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("ADICIONAR") }
            OutlinedButton(onClick = {
                if (lista.isEmpty()) {
                    dispararNotificacaoImediata(context, "Atenção", "Nenhum remédio cadastrado.")
                } else {
                    val resumo = StringBuilder("Lembretes:\n\n")
                    lista.forEach { resumo.append("💊 ${it.nome} (${it.dosagem}) às ${it.horario}\n") }
                    dispararNotificacaoImediata(context, "CuidaIdoso", resumo.toString())
                }
            }, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("TESTAR") }
        }
        LazyColumn {
            items(lista) { med ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    ListItem(
                        headlineContent = { Text(med.nome, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${med.dosagem} às ${med.horario}${if(med.isRecorrente) " (Diário)" else ""}") },
                        trailingContent = {
                            Row {
                                IconButton({
                                    editandoMed = med
                                    showDialog = true
                                }) { Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary) }
                                IconButton({
                                    lista.remove(med)
                                    historico.add(0, HistoricoEvento("Removido: ${med.nome}", obterDataHoraAtual(), Icons.Default.DeleteForever))
                                }) { Icon(Icons.Default.Delete, "Excluir", tint = Color.Red) }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button({
                    if (nomeMed.isNotEmpty()) {
                        val novoMed = Medicamento(nomeMed, horaFieldValue.text, doseMed, isRecorrente)
                        if (editandoMed != null) {
                            val index = lista.indexOf(editandoMed)
                            if (index != -1) lista[index] = novoMed
                            historico.add(0, HistoricoEvento("Editado: $nomeMed", obterDataHoraAtual(), Icons.Default.EditNote))
                        } else {
                            lista.add(novoMed)
                            historico.add(0, HistoricoEvento("Adicionado: $nomeMed", obterDataHoraAtual(), Icons.Default.AddCircle))
                        }
                        agendarNotificacaoMedicamento(context, novoMed)
                        showDialog = false
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ showDialog = false }) { Text("Cancelar") } },
            title = { Text(if (editandoMed == null) "Novo Medicamento" else "Editar Medicamento") },
            text = {
                Column {
                    OutlinedTextField(nomeMed, { nomeMed = it }, label = { Text("Nome do Remédio") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = horaFieldValue,
                        onValueChange = { input ->
                            val d = input.text.filter { it.isDigit() }
                            if (d.length <= 4) {
                                var finalStr = d
                                if (d.length >= 2) {
                                    val h = d.substring(0, 2).toIntOrNull() ?: 0
                                    val hVal = if (h > 23) "23" else d.substring(0, 2)
                                    finalStr = hVal + d.substring(2)
                                }
                                if (d.length == 4) {
                                    val m = d.substring(2, 4).toIntOrNull() ?: 0
                                    if (m > 59) finalStr = finalStr.substring(0, 2) + "59"
                                }
                                val fmt = if (finalStr.length >= 3) finalStr.substring(0, 2) + ":" + finalStr.substring(2) else finalStr
                                horaFieldValue = TextFieldValue(fmt, TextRange(fmt.length))
                            }
                        },
                        label = { Text("Hora (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(doseMed, { doseMed = it }, label = { Text("Dose") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(isRecorrente, { isRecorrente = it })
                        Text("Uso recorrente")
                    }
                }
            }
        )
    }
}

@Composable
fun AgendaConsultasScreen(lista: MutableList<Consulta>, historico: MutableList<HistoricoEvento>) {
    var showDialog by remember { mutableStateOf(false) }
    var editandoConsulta by remember { mutableStateOf<Consulta?>(null) }

    var tipo by remember { mutableStateOf("Consulta") }
    var espec by remember { mutableStateOf("") }
    var dataVal by remember { mutableStateOf(TextFieldValue("")) }
    var horaVal by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            tipo = editandoConsulta?.tipo ?: "Consulta"
            espec = editandoConsulta?.especialidade ?: ""
            val d = editandoConsulta?.data ?: ""
            val h = editandoConsulta?.hora ?: ""
            dataVal = TextFieldValue(d, TextRange(d.length))
            horaVal = TextFieldValue(h, TextRange(h.length))
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Agenda", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button({
            editandoConsulta = null
            showDialog = true
        }, Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("MARCAR NOVO") }
        LazyColumn {
            items(lista) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    ListItem(
                        headlineContent = { Text(item.especialidade, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${item.tipo} - ${item.data} às ${item.hora}") },
                        trailingContent = {
                            Row {
                                IconButton({
                                    editandoConsulta = item
                                    showDialog = true
                                }) { Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary) }
                                IconButton({
                                    lista.remove(item)
                                    historico.add(0, HistoricoEvento("Cancelado: ${item.especialidade}", obterDataHoraAtual(), Icons.Default.EventBusy))
                                }) { Icon(Icons.Default.Delete, "Excluir") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button({
                    if (espec.isNotEmpty()) {
                        val nova = Consulta(tipo, espec, dataVal.text, horaVal.text)
                        if (editandoConsulta != null) {
                            val index = lista.indexOf(editandoConsulta)
                            if (index != -1) lista[index] = nova
                            historico.add(0, HistoricoEvento("Atualizado: $espec", obterDataHoraAtual(), Icons.Default.EditCalendar))
                        } else {
                            lista.add(nova)
                            historico.add(0, HistoricoEvento("Agendado: $espec", obterDataHoraAtual(), Icons.Default.EventAvailable))
                        }
                        showDialog = false
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton({ showDialog = false }) { Text("Voltar") } },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(tipo == "Consulta", { tipo = "Consulta" }); Text("Consulta")
                        RadioButton(tipo == "Exame", { tipo = "Exame" }); Text("Exame")
                    }
                    OutlinedTextField(espec, { espec = it }, label = { Text("Especialidade (Ex: Geriatra)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = dataVal,
                        onValueChange = { input ->
                            val d = input.text.filter { it.isDigit() }
                            if (d.length <= 4) {
                                var finalStr = d
                                if (d.length >= 2) {
                                    val dia = d.substring(0, 2).toIntOrNull() ?: 0
                                    val diaVal = if (dia > 31) "31" else d.substring(0, 2)
                                    finalStr = diaVal + d.substring(2)
                                }
                                if (d.length == 4) {
                                    val mes = d.substring(2, 4).toIntOrNull() ?: 0
                                    if (mes > 12) finalStr = finalStr.substring(0, 2) + "12"
                                }
                                val fmt = if (finalStr.length >= 3) finalStr.substring(0, 2) + "/" + finalStr.substring(2) else finalStr
                                dataVal = TextFieldValue(fmt, TextRange(fmt.length))
                            }
                        },
                        label = { Text("Data (DD/MM)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = horaVal,
                        onValueChange = { input ->
                            val d = input.text.filter { it.isDigit() }
                            if (d.length <= 4) {
                                var finalStr = d
                                if (d.length >= 2) {
                                    val h = d.substring(0, 2).toIntOrNull() ?: 0
                                    val hVal = if (h > 23) "23" else d.substring(0, 2)
                                    finalStr = hVal + d.substring(2)
                                }
                                if (d.length == 4) {
                                    val m = d.substring(2, 4).toIntOrNull() ?: 0
                                    if (m > 59) finalStr = finalStr.substring(0, 2) + "59"
                                }
                                val fmt = if (finalStr.length >= 3) finalStr.substring(0, 2) + ":" + finalStr.substring(2) else finalStr
                                horaVal = TextFieldValue(fmt, TextRange(fmt.length))
                            }
                        },
                        label = { Text("Hora (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        )
    }
}

@Composable
fun ContatosEmergenciaScreen(lista: MutableList<ContatoEmergencia>, historico: MutableList<HistoricoEvento>) {
    var showDialog by remember { mutableStateOf(false) }
    var editandoContato by remember { mutableStateOf<ContatoEmergencia?>(null) }

    var nome by remember { mutableStateOf("") }
    var telVal by remember { mutableStateOf(TextFieldValue("")) }
    var parent by remember { mutableStateOf("") }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            nome = editandoContato?.nome ?: ""
            val t = editandoContato?.telefone ?: ""
            telVal = TextFieldValue(t, TextRange(t.length))
            parent = editandoContato?.parentesco ?: ""
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Contatos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button({
            editandoContato = null
            showDialog = true
        }, Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("ADICIONAR CONTATO") }
        LazyColumn {
            items(lista) { contato ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    ListItem(
                        headlineContent = { Text(contato.nome, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${contato.parentesco} - ${contato.telefone}") },
                        trailingContent = {
                            Row {
                                IconButton({
                                    editandoContato = contato
                                    showDialog = true
                                }) { Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary) }
                                IconButton({
                                    lista.remove(contato)
                                    historico.add(0, HistoricoEvento("Excluído: ${contato.nome}", obterDataHoraAtual(), Icons.Default.PersonRemove))
                                }) { Icon(Icons.Default.Delete, "Excluir") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button({
                    if (nome.isNotEmpty()) {
                        val novo = ContatoEmergencia(nome, telVal.text, parent)
                        if (editandoContato != null) {
                            val index = lista.indexOf(editandoContato)
                            if (index != -1) lista[index] = novo
                            historico.add(0, HistoricoEvento("Editado: $nome", obterDataHoraAtual(), Icons.Default.ContactPage))
                        } else {
                            lista.add(novo)
                            historico.add(0, HistoricoEvento("Salvo: $nome", obterDataHoraAtual(), Icons.Default.PersonAdd))
                        }
                        showDialog = false
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ showDialog = false }) { Text("Cancelar") } },
            text = {
                Column {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = telVal,
                        onValueChange = { input ->
                            val d = input.text.filter { it.isDigit() }
                            if (d.length <= 11) {
                                val fmt = when {
                                    d.length > 10 -> "(${d.substring(0, 2)}) ${d.substring(2, 7)}-${d.substring(7)}"
                                    d.length > 6 -> "(${d.substring(0, 2)}) ${d.substring(2, 6)}-${d.substring(6)}"
                                    d.length > 2 -> "(${d.substring(0, 2)}) ${d.substring(2)}"
                                    d.length > 0 -> "(${d}"
                                    else -> d
                                }
                                telVal = TextFieldValue(fmt, TextRange(fmt.length))
                            }
                        },
                        label = { Text("Telefone (DDD + Número)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(parent, { parent = it }, label = { Text("Parentesco (Ex: Avo)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }
}

@Composable
fun HistoricoTimelineScreen(historico: List<HistoricoEvento>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Histórico", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
        items(historico) { ev ->
            ListItem(
                headlineContent = { Text(ev.titulo) },
                supportingContent = { Text(ev.dataHora) },
                leadingContent = { Icon(ev.icone, null, tint = Color(0xFF1976D2)) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun PerfilUsuarioScreen(usuario: Usuario, onLogout: () -> Unit) {
    var nome by remember { mutableStateOf(usuario.nome) }
    var email by remember { mutableStateOf(usuario.email) }
    var senha by remember { mutableStateOf(usuario.senha) }
    var editando by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, enabled = editando, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, enabled = editando, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        if (editando) OutlinedTextField(senha, { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(24.dp))
        if (!editando) Button({ editando = true }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("EDITAR PERFIL") }
        else Button({
            usuario.nome = nome; usuario.email = email; usuario.senha = senha; editando = false
        }, Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) { Text("SALVAR") }
        Spacer(Modifier.height(16.dp))
        Button(onLogout, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) { Text("SAIR") }
    }
}