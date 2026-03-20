package br.com.contarim.cuidaidoso

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

class NotificacaoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "CuidaIdoso"
        val mensagem = intent.getStringExtra("mensagem") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cuidaidoso_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Saúde",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes de medicamentos e consultas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

fun agendarNotificacaoMedicamento(context: Context, medicamento: Medicamento) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificacaoReceiver::class.java).apply {
            putExtra("titulo", "Hora do Remédio")
            putExtra("mensagem", "Tomar ${medicamento.nome} (${medicamento.dosagem})")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamento.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val partesHora = medicamento.horario.split(":")
        if (partesHora.size == 2) {
            val hora = partesHora[0].toInt()
            val minuto = partesHora[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, minuto)
                set(Calendar.SECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            if (medicamento.isRecorrente) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                // Removida a checagem desnecessária de SDK_INT >= 28
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun dispararNotificacaoImediata(context: Context, titulo: String, mensagem: String) {
    val intent = Intent(context, NotificacaoReceiver::class.java).apply {
        putExtra("titulo", titulo)
        putExtra("mensagem", mensagem)
    }
    context.sendBroadcast(intent)
}