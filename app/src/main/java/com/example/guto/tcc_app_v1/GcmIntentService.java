package com.example.guto.tcc_app_v1;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 *  Classe para tratar da mensagem GCM
 */
public class GcmIntentService extends IntentService {

    private  String nomePaciente="";
    private  String idPaciente="";
    private  String idMedico="";
    private  String mensagem="";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        nomePaciente = extras.getString("nomePaciente");
        idPaciente = extras.getString("idPaciente");
        mensagem =extras.getString("message");

        // Pega idMedico salvo nas preferencias do app
       final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        idMedico = prefs.getString("idMedico", "");

        if (!extras.isEmpty()) {

            // Verifica o tipo da mensagem
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) { // Erro
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) { // Delete
                sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) { // Mensagem: nessa categoria que se tem interesse

               // Pode ser realizada alguma atividade em background posteriormente

                // Posta uma notificação com a mensagem recebida
                sendNotification("" + mensagem);

            }
        }
        // Libera o wakelock
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Coloca a mensagem em uma notificação e exibe ela
    private void sendNotification(String msg) {

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);


        // Cria um intent para PacienteActivity quando clicar na notificação
        Intent intentToPacienteActivity = new Intent (this, PacienteActivity.class);
        intentToPacienteActivity.putExtra("idPaciente", idPaciente);
        intentToPacienteActivity.putExtra("nomePaciente", nomePaciente);
        intentToPacienteActivity.putExtra("idMedico", idMedico);


          PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                  intentToPacienteActivity, PendingIntent.FLAG_UPDATE_CURRENT);


        // Cria a notificação
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.alert_icon)
                .setContentTitle(nomePaciente)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg);


        // Vibracao
        mBuilder.setVibrate(new long[]{0, 1000, 1000, 1000, 1000});
        // LED
        mBuilder.setLights(Color.RED, 1000,1000);
        // Som da notificação
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        // Após clicar na notificação ela some
        mBuilder.setAutoCancel(true);

        // Define o intent para quando clicar na notificação
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());


    }
}
