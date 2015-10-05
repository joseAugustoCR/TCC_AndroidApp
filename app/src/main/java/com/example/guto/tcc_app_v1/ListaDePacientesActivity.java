package com.example.guto.tcc_app_v1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *  Classe para exibir uma lista com todos os pacientes do médico que se logou no app
 */
public class ListaDePacientesActivity extends Activity implements  AsyncResponse{
    private ListView pacientesListView;
    private  SendData  sendData;
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private List<ListViewItem> itens = new ArrayList<ListViewItem>();
    private  String idMedico;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega o dado passado por intent
        Intent intent = getIntent();
        boolean logout = getIntent().getBooleanExtra("logout", false);
        // Se logout=true, vai para LoginActivity e termina essa activity
        if (logout){
            Intent intentToLogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentToLogin);
            finish();
            return;
        }

        // Pega idMedico do intent
        idMedico = intent.getStringExtra("idMedico");

        setContentView(R.layout.activity_patients);
        pacientesListView = (ListView) findViewById(R.id.patientsListView);


        // Coloca a tag e o idMedico em List<NameValuePair> data
       data.add(new BasicNameValuePair("tag","getPacientes"));
       data.add(new BasicNameValuePair("idMedico", idMedico));

        // Envia para o servidor
        sendData = new SendData(  data, getApplicationContext());
        sendData.delegate = ListaDePacientesActivity.this;       // Método para tratar da resposta do servidor
        sendData.execute();

        // Ao clicar sobre um paciente, faz um intent para PacienteActivity
        pacientesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = (ListViewItem) pacientesListView.getItemAtPosition(position);

                Intent intentToPacienteActivity = new Intent (getApplication(), PacienteActivity.class);
                intentToPacienteActivity.putExtra("idPaciente", item.id);
                intentToPacienteActivity.putExtra("nomePaciente", item.nome);
                intentToPacienteActivity.putExtra("idMedico", idMedico);
                startActivity(intentToPacienteActivity);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id ==R.id.logout){     // Selecionou logout




            // Gera aviso de confirmação
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_action_warning)
                    .setTitle("Log out")
                    .setMessage("Tem certeza que deseja fazer log out do app?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Desregistra GCM
                            Context context = getApplicationContext();
                            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
                            try {
                                gcm.unregister();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Apaga preferencias
                            final SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear();
                            editor.commit();

                            deleteRegistrationIdFromBackend();
                        }

                    })
                    .setNegativeButton("Não", null)
                    .show();

            return true;


        }

        return super.onOptionsItemSelected(item);
    }


    private void deleteRegistrationIdFromBackend() {

        // Coloca tag, idMedico e regid em List<NameValuePair> data
        data.add(new BasicNameValuePair("tag", "reg_id"));
        data.add(new BasicNameValuePair("idMedico", idMedico));
        data.add(new BasicNameValuePair("reg_id", "NULL"));

        // Envia para o servidor
        sendData = new SendData( data, getApplicationContext());
        sendData.delegate = ListaDePacientesActivity.this;       // Método para tratar da resposta do servidor
        sendData.execute();
    }


    // Método para tratar da resposta do servidor
    @Override
    public void sendFinish(String output) {

       try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
           String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");

           if (tag.equals("getPacientes"))    // Resposta da requisição dos pacientes
           {
               if (!error) {
                   JSONObject object = response.getJSONObject("pacientes");

                   Iterator<String> iter = object.keys();
                   while (iter.hasNext()) {                                                // Iteração entre os pacientes do array
                       String key = iter.next();
                       try {
                           // Decodifica resposta
                           JSONObject patientObject = object.getJSONObject(key);
                           String nome = patientObject.getString("nome");
                           String idPaciente = patientObject.getString("idPaciente");

                        // Adiciona paciente a lista
                           ListViewItem item = new ListViewItem();
                           item.nome = nome;
                           item.id = idPaciente;

                           itens.add(item);

                           Log.v("LogTag", nome + " " + idPaciente);

                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }

                   // Preenche listView com os pacientes da lista
                   ArrayAdapter<ListViewItem> arrayAdapter = new ArrayAdapter<ListViewItem>(ListaDePacientesActivity.this, android.R.layout.simple_list_item_1, itens);
                   arrayAdapter.sort(new Comparator<ListViewItem>() {
                       @Override
                       public int compare(ListViewItem lhs, ListViewItem rhs) { // Ordem alfabética
                           return lhs.nome.compareTo (rhs.nome);
                       }
                   });

                   pacientesListView.setAdapter(arrayAdapter);

               } else {
                   String errorMsg = response.getString("erro_msg");
                   Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
               }
           }
           else if (tag.equals("reg_id"))    // Resposta apagou regId
           {
               if (!error) {  // Apagou regId corretamente

                   // Intent para login e termina essa activity
                   Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent);
                   finish();

               }else{
                   String errorMsg = response.getString("erro_msg");
                   Toast.makeText(getApplicationContext(),  errorMsg, Toast.LENGTH_LONG).show();
                }
           }

        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }

    }


    /**
     *  Classe interna para preencher lsitView
     */
    private class ListViewItem{
        String nome;
        String id;

        @Override
        public String toString() {
            return nome;
        }
    }

}


