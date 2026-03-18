package br.com.contarim.cuidaidoso

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import br.com.contarim.cuidaidoso.ui.theme.CuidaIdosoTheme

enum class Destino { BoasVindas, AuthOpcoes, Login, Registro, AppPrincipal }

class MainActivity : ComponentActivity() {
    companion object {
        val listaUsuariosGlobal = mutableStateListOf<Usuario>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verificarPermissaoAlarmeExato()

        setContent {
            CuidaIdosoTheme {
                MainContent()
            }
        }
    }

    private fun verificarPermissaoAlarmeExato() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MainContent() {
    val listaMed = remember { mutableStateListOf<Medicamento>() }
    val listaContatos = remember { mutableStateListOf<ContatoEmergencia>() }
    val listaHistorico = remember { mutableStateListOf<HistoricoEvento>() }
    val listaConsultas = remember { mutableStateListOf<Consulta>() }

    var usuarioLogado by remember { mutableStateOf<Usuario?>(null) }
    var telaAtual by remember { mutableStateOf(Destino.BoasVindas) }
    var selectedTab by remember { mutableStateOf(0) }

    when (telaAtual) {
        Destino.BoasVindas -> WelcomeScreen { telaAtual = Destino.AuthOpcoes }

        Destino.AuthOpcoes -> AuthScreen(
            onLoginClick = { telaAtual = Destino.Login },
            onRegisterClick = { telaAtual = Destino.Registro }
        )

        Destino.Login -> LoginScreen(
            // Removido o qualificador redundante 'MainActivity.'
            usuarios = MainActivity.listaUsuariosGlobal,
            onBack = { telaAtual = Destino.AuthOpcoes },
            onLoginSuccess = { user ->
                usuarioLogado = user
                telaAtual = Destino.AppPrincipal
            }
        )

        Destino.Registro -> RegistroScreen(
            onBack = { telaAtual = Destino.AuthOpcoes },
            onRegisterSuccess = { novoUser ->
                MainActivity.listaUsuariosGlobal.add(novoUser)
                usuarioLogado = novoUser
                telaAtual = Destino.AppPrincipal
            }
        )

        Destino.AppPrincipal -> {
            usuarioLogado?.let { user ->
                AppScaffold(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    listaMed = listaMed,
                    listaContatos = listaContatos,
                    listaHistorico = listaHistorico,
                    listaConsultas = listaConsultas,
                    usuario = user,
                    onLogout = {
                        usuarioLogado = null
                        telaAtual = Destino.AuthOpcoes
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
fun AppScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    listaMed: MutableList<Medicamento>,
    listaContatos: MutableList<ContatoEmergencia>,
    listaHistorico: MutableList<HistoricoEvento>,
    listaConsultas: MutableList<Consulta>,
    usuario: Usuario,
    onLogout: () -> Unit
) {
    val items = listOf("Remédios", "Agenda", "Contatos", "Histórico", "Conta")
    val icons = listOf(
        Icons.Default.Medication,
        Icons.Default.Event,
        Icons.AutoMirrored.Filled.PhoneCallback,
        Icons.Default.Timeline,
        Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ListaMedicamentosScreen(listaMed, listaHistorico)
                1 -> AgendaConsultasScreen(listaConsultas, listaHistorico)
                2 -> ContatosEmergenciaScreen(listaContatos, listaHistorico)
                3 -> HistoricoTimelineScreen(listaHistorico)
                4 -> PerfilUsuarioScreen(usuario, onLogout)
            }
        }
    }
}