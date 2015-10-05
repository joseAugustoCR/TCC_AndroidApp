package com.example.guto.tcc_app_v1;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *  Fragment para registrar um diagnóstico
 */
public class DiagnosticarFragment extends Fragment implements AsyncResponse{
    private  SendData  sendData;
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  String idPaciente;
    private  Spinner diagnosticosSpinner;
    private  EditText observacoesEditText;
    private  Button registraDiagnosticoButton;
    private DrawerLayout mDrawerLayout;


    public DiagnosticarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega idPaciente do intent
        idPaciente = getActivity().getIntent().getStringExtra("idPaciente");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diagnosticar, container, false);

        diagnosticosSpinner = (Spinner)view.findViewById(R.id.diagnosticosSpinner);
        observacoesEditText = (EditText)view.findViewById(R.id.observacoesEditText);
        registraDiagnosticoButton = (Button)view.findViewById(R.id.registraDiagnosticoButton);

        // Define foco no spinner
        diagnosticosSpinner.setFocusable(true);
        diagnosticosSpinner.setFocusableInTouchMode(true);


        DBTools dbTools = new DBTools(getActivity());

        List <SpinnerItem>  diagnosticosList = dbTools.getCID();

        // Adiciona item de Hint
        SpinnerItem hint = new SpinnerItem();
        hint.nome = " Selecione um diagnóstico...";
        hint.id = "NULL";
        diagnosticosList.add(hint);

        // Preenche o spinner com os dados do SQLite DB
        ArrayAdapter<SpinnerItem> arrayAdapter = new ArrayAdapter<SpinnerItem>(getActivity(), android.R.layout.simple_list_item_1, diagnosticosList);
        arrayAdapter.sort(new Comparator<com.example.guto.tcc_app_v1.SpinnerItem>() {
            @Override
            public int compare(com.example.guto.tcc_app_v1.SpinnerItem lhs, com.example.guto.tcc_app_v1.SpinnerItem rhs) { // Ordem alfabética
                return lhs.nome.compareTo(rhs.nome);
            }
        });

        diagnosticosSpinner.setAdapter(arrayAdapter);


        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        // Clique do botão Registra
        registraDiagnosticoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pega dados dos widgets
                String observacoes = observacoesEditText.getText().toString();
                SpinnerItem item = (SpinnerItem)diagnosticosSpinner.getSelectedItem();
                String idDiagnostico = item.id;

                Log.d("log_tag", "id diagnostico = " + idDiagnostico);

                // Coloca tag, idPaciente e idCID em  List<NameValuePair> data
                data.add(new BasicNameValuePair("tag", "registraDiagnostico"));
                data.add(new BasicNameValuePair("idPaciente", idPaciente));
                data.add(new BasicNameValuePair("idDiagnostico", idDiagnostico));
                data.add(new BasicNameValuePair("observacoes", observacoes));

                // Envia para o servidor
                sendData = new SendData( data, getActivity().getApplicationContext());
                sendData.delegate = DiagnosticarFragment.this;       // Define método para manusear resposta do servidor
                sendData.execute();
            }
        });

        return view;
    }

    // Método para pegar resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {

            JSONObject response = new JSONObject(output);         // Pega o resultado
            String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");

            if (tag.equals("registraDiagnostico")) {    // Resposta do salvamento de uma diagnóstico

                if (!error){    // Salvou corretamente
                    Toast.makeText(getActivity(), "Diagnóstico Registrado!", Toast.LENGTH_LONG).show();
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                    // Limpa campos
                    diagnosticosSpinner.setSelection(0);
                    observacoesEditText.setText("");

                }else{
                    Log.e("log_tag", "Erro ao registrar diagnostico");
                    String errorMsg = response.getString("erro_msg");
                    Toast.makeText(getActivity(),  errorMsg, Toast.LENGTH_LONG).show();
                }
            }


        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }
    }


}
