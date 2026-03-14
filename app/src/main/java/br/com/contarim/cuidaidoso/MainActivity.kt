package br.com.contarim.cuidaidoso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import br.com.contarim.cuidaidoso.ui.theme.CuidaIdosoTheme

// Configuração de destino para navegação segura
enum class Destino { BoasVindas, AuthOpcoes, Login, Registro, AppPrincipal }

class MainActivity : ComponentActivity() {
    companion object {
        // Persistência em memória dos usuários cadastrados
        val listaUsuariosGlobal = mutableStateListOf<Usuario>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CuidaIdosoTheme {
                MainContent()
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
        Destino.AuthOpcoes -> AuthScreen({ telaAtual = Destino.Login }, { telaAtual = Destino.Registro })

        Destino.Login -> LoginScreen(MainActivity.listaUsuariosGlobal, { telaAtual = Destino.AuthOpcoes }) { user ->
            usuarioLogado = user
            telaAtual = Destino.AppPrincipal
        }

        Destino.Registro -> RegistroScreen({ telaAtual = Destino.AuthOpcoes }) { novoUser ->
            MainActivity.listaUsuariosGlobal.add(novoUser)
            usuarioLogado = novoUser
            telaAtual = Destino.AppPrincipal
        }

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
    selectedTab: Int, onTabSelected: (Int) -> Unit,
    listaMed: MutableList<Medicamento>,
    listaContatos: MutableList<ContatoEmergencia>,
    listaHistorico: MutableList<HistoricoEvento>,
    listaConsultas: MutableList<Consulta>,
    usuario: Usuario, onLogout: () -> Unit
) {
    val items = listOf("Remédios", "Agenda", "Contatos", "Histórico", "Conta")
    val icons = listOf(Icons.Default.Medication, Icons.Default.Event, Icons.Default.PhoneCallback, Icons.Default.Timeline, Icons.Default.Person)

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