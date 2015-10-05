package com.example.guto.tcc_app_v1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Activity para realizar login no app
 */
public class LoginActivity extends Activity implements AsyncResponse {
    public  static String PROPERTY_REG_ID = "registration_id";
    private  static final String PROPERTY_APP_VERSION = "appVersion";
    private static final  String ID_MEDICO = "idMedico";
    private  static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  EditText loginEditText;
    private  EditText senhaEditText;
    private  Button loginButton;
    private  String login;
    private  String senha;
    private  String idMedico;
    private  SendData sendData;
    DBTools dbTools = new DBTools(this);
    // Sender ID do GCM
    String SENDER_ID = "134313553104";
    GoogleCloudMessaging gcm;
    Context context;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEditText = (EditText) findViewById(R.id.loginEditText);
        senhaEditText = (EditText) findViewById(R.id.senhaEditText);
        loginButton = (Button) findViewById(R.id.loginButton);

        context = getApplicationContext();

        // Clique do botão login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                login = loginEditText.getText().toString();
                senha = senhaEditText.getText().toString();

                if (login.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Digite login e senha", Toast.LENGTH_SHORT).show();
                } else {

                    ProgressDialog.show(LoginActivity.this, "Carregando", "Aguarde...");

                    // Coloca login, senha e tag na lista de dados para enviar ao servidor
                    data.add(new BasicNameValuePair("tag", "login"));
                    data.add(new BasicNameValuePair("login", login));
                    data.add(new BasicNameValuePair("senha", senha));

                    // Envia dados para o servidor
                    sendData = new SendData(  data, context);
                    sendData.delegate = LoginActivity.this;       // Método para tratar a resposta do servidor
                    sendData.execute();
                }



            }
        });




        // Verifica play serevices
        if (checkPlayServices()) {

            gcm = GoogleCloudMessaging.getInstance(this);
            // Obtem reg_id salvo no dispositivo
            regid = getData(context);


            if (!regid.isEmpty()) { // Se houver dados salvos no dispositivo, nao precisa fazer login


                context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
                context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));

                // Intent para PacienteActivity:
                Intent intentToPatients = new Intent(getApplication(), ListaDePacientesActivity.class);
                intentToPatients.putExtra("idMedico", idMedico);
                startActivity(intentToPatients);
                finish(); // nao mostra activity login novamente

            }
        } else {
            Log.i("log_tag", "No valid Google Play Services APK found.");
            Toast.makeText(getApplicationContext(), "Google Play Services não encontrado! GCM não funcionará !", Toast.LENGTH_LONG).show();
        }



    }



  // Função para verificar se a play services está disponível no dispositivo
    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Log.i("log_tag", "This device is not supported.");
                finish();
            }
            return false;
        }

        return true;
    }



 // Salva reg id e idMedico nas preferencias do app após o login, para não precisar inserir as informções novamente
    private void storeData(Context context, String regId) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("log_tag", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();

        // Salva reg_id, appVersion e idMedico
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.putString(ID_MEDICO, idMedico);
        editor.commit();
    }


  // Obtém os dados salvos nas preferencias do ap
    private String getData(Context context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("log_tag", "Registration not found.");
            return "";
        }
        // Pega idMedico salvo
        idMedico = prefs.getString(ID_MEDICO, "");

      // Verifica versão do app
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("log_tag", "App version changed.");
            return "";
        }
        return registrationId;
    }


    // Registra o dispositivo com o GCM e após envia o regId para o servidor para ser armazenado
    // no banco de dados externo MySQL
    private void registerInBackground() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // Salva dados no dispositivo
                    storeData(context, regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // Envia redId para o servidor (armazenar no DB)
                sendRegistrationIdToBackend();
                context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
                context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

// Versão do app
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


 // retorna Shared Preferences do app
    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences(LoginActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    // Função para fazer o envio do regId para o servidor
    private void sendRegistrationIdToBackend() {

        // Coloca tag, idMedico e regid em List<NameValuePair> data
        data.add(new BasicNameValuePair("tag", "reg_id"));
        data.add(new BasicNameValuePair("idMedico", idMedico));
        data.add(new BasicNameValuePair("reg_id", regid));

        // Envia para o servidor
        sendData = new SendData(  data, context);
        sendData.delegate = LoginActivity.this;       // Método para tratar a resposta do servidor
        sendData.execute();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // Método para tratar a resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {

            JSONObject response = new JSONObject(output);         // Pega o resultado
            String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");

            if (tag.equals("login"))    // Resposta da verificacao de login
            {
                if (!error){    // Login deu certo
                    JSONObject doctor = response.getJSONObject("user");
                    idMedico = doctor.getString("idUsuario");
                    Log.d("log_tag", "Login idMedico: " + idMedico);

                    // Registra o dispositivo no GCM, salva os dados no dispositivo e envia regId para o servidor
                    registerInBackground();

                    // Atualiza DB interno
                    dbTools.updateDB();

                    Log.d("log_tag", "final login: " + idMedico);

                } else{
                    String errorMsg = response.getString("erro_msg");
                    Toast.makeText(getApplicationContext(),  errorMsg, Toast.LENGTH_LONG).show();
                }

            }else if (tag.equals("reg_id")) {    // Resposta do salvamento de regid no banco de dados

                if (!error){  // Salvou regId corretamento

                    // Intent para PacienteActivity:
                    Intent intentToPatients = new Intent (getApplication(), ListaDePacientesActivity.class);
                    intentToPatients.putExtra("idMedico", idMedico);
                    startActivity(intentToPatients);
                    finish(); // nao mostra activity login novamente
                }else{
                    String errorMsg = response.getString("erro_msg");
                    Toast.makeText(getApplicationContext(),  errorMsg, Toast.LENGTH_LONG).show();
                }
            }


        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }

    }

}
