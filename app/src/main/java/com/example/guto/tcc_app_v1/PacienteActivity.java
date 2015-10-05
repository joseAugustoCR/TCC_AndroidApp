package com.example.guto.tcc_app_v1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Activity do paciente
 *  Implementa um NavigationDrawer e substitui os Fragments de acordo com a opção selecionada, sendo responsável por conter todas as funcionalidades do app
 */
public class PacienteActivity extends AppCompatActivity  implements NavigationDrawerFragment.NavigationDrawerCallbacks, AsyncResponse {
    private  String idPaciente="";
    private  String idMedico="";
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  SendData  sendData;
    // Fragment do navigation drawer
    private NavigationDrawerFragment mNavigationDrawerFragment;
    // Título da action bar
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente);

        // Pega o dado passado por intent
        Intent intent = getIntent();
        idPaciente = intent.getStringExtra("idPaciente");
        idMedico = intent.getStringExtra("idMedico");

        Log.v("log_tag", "idPaciente chegou pelo intent "+idPaciente);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mTitle = getString(R.string.title_section1);


        // Configura o drawer
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    // Ao chegar um novo intent (da notificação), define o intent que chegou como o principal
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v("log_tag", "OnNewIntent " + intent.getStringExtra("idPaciente"));
       PacienteActivity.this.setIntent(intent);

        // Reinicia o fragment de informação do paciente com os dados do novo paciente (da notificação)
        onNavigationDrawerItemSelected(0);

    }

    // Ao Selecionar uma opção do NavigationDrawer
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // Atualiza o conteúdo princical da activity substituindo os fragments e definindo o novo título
        Fragment objFragment = null;

        switch(position){
            case 0:
                mTitle = getString(R.string.title_section1);
                objFragment = new InformacoesFragment();
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                objFragment = new MonitorarFragment();
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                objFragment = new HistoricoFragment();
                break;
            case 3:
                mTitle = getString(R.string.title_section4);
                objFragment = new DiagnosticarFragment();
                break;
            case 4:
                mTitle = getString(R.string.title_section5);
                objFragment = new EvolucaoFragment();
                break;
            case 5:
                mTitle = getString(R.string.title_section6);
                objFragment = new RequisicaoFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container,objFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.paciente, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Ao selecionar itens do menu daactivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.logout){    // Selecionar logout

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

                            // Chama função para apagar o regId salvo no MySQL
                            deleteRegistrationIdFromBackend();
                        }

                    })
                    .setNegativeButton("Não", null)
                    .show();




            return true;

        }

        return super.onOptionsItemSelected(item);
    }


    // Apaga o regId do médico salvo no MySQL
    private void deleteRegistrationIdFromBackend() {

        // Coloca tag, idMedico e regid em List<NameValuePair> data
        data.add(new BasicNameValuePair("tag", "reg_id"));
        data.add(new BasicNameValuePair("idMedico", idMedico));
        data.add(new BasicNameValuePair("reg_id", "NULL"));

        // Envia para o servidor
        sendData = new SendData(  data, getApplicationContext());
        sendData.delegate = PacienteActivity.this;       // Método para tratar da resposta do servidor
        sendData.execute();
    }


    // Método para tratar da resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
            String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");

            if (tag.equals("reg_id"))    // Resposta apagou regId
            {
                if (!error) {  // Apagou regId corretamente

                    // Intent para Activity anterior passsando um comando de logout e termina essa activity
                    Intent intent = new Intent(getApplicationContext(), ListaDePacientesActivity.class);
                    intent.putExtra("logout", true); // Avisa que logout foi solicitado
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = response.getString("erro_msg");
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_paciente, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((PacienteActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }



}
