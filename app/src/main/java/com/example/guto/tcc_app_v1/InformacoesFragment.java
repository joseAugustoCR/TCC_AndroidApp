package com.example.guto.tcc_app_v1;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Fragment para exibir as informações do paciente
 */
public class InformacoesFragment extends Fragment implements AsyncResponse {
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  SendData  sendData;
    private  TextView nomeTextView;
    private  TextView dataDeNascimentoTextView;
    private  TextView descricaoTextView;
    private  TextView diagnosticoTextView;
    private  TextView quartoTextView;
    private  TextView dataDeAdmissaoTextView;
    private  String idPaciente;


    public InformacoesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega idPaciente do intent
        idPaciente = getActivity().getIntent().getStringExtra("idPaciente");

        // Coloca tag e idPaciente em List<NameValuePair> data
        data.add(new BasicNameValuePair("tag", "getPaciente"));
        data.add(new BasicNameValuePair("idPaciente", idPaciente));

        // Envia para o servidor
        sendData = new SendData( data, getActivity().getApplicationContext());
        sendData.delegate = InformacoesFragment.this;       // Método para tratar da resposta do servidor
        sendData.execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_informacoes, container, false);

        nomeTextView = (TextView)view.findViewById(R.id.nomeTextView);
        dataDeNascimentoTextView = (TextView)view.findViewById(R.id.dataNascimentoTextView);
        descricaoTextView = (TextView)view.findViewById(R.id.descricaoTextView);
        quartoTextView = (TextView)view.findViewById(R.id.quartoTextView);
        dataDeAdmissaoTextView = (TextView)view.findViewById(R.id.dataDeAdmissaoTextView);
        diagnosticoTextView = (TextView)view.findViewById(R.id.diagnosticoTextView);

        return view;

    }

    // Método para tratar da resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
            boolean error = response.getBoolean("erro");

            if (!error) {   // Se não houve erro
                // Decodifica a resposta
                JSONObject object = response.getJSONObject("paciente");
                String nome = object.getString("nome");
                String dataDeNascimento = object.getString("dataDeNascimento");
                String descricao = object.getString("descricao");
                String quarto = object.getString("quarto");
                String dataDeAdmissao = object.getString("dataDeAdmissao");
                String diagnostico = "";

                try {
                    JSONObject diagnosticosObject = object.getJSONObject("diagnostico");
                    String virgula = "";

                    // Laço para pegar todas as doenças dentro do array de diagnósticos
                    Iterator<String> iter = diagnosticosObject.keys();
                    String key;
                    JSONObject diagnosticoObject;
                    while (iter.hasNext()) {                                                // Iteração entre diagnosticos
                         key = iter.next();
                        diagnosticoObject = diagnosticosObject.getJSONObject(key);
                        diagnostico += virgula + diagnosticoObject.getString("nome");
                        virgula = ", ";
                    }
                }catch (JSONException e){
                    Log.e("log_tag", "Erro parsin diagnostico " + e.toString());
                }

                // Define o texto dos widgets
                nomeTextView.setText(nome);
                dataDeNascimentoTextView.setText(dataDeNascimento);
                descricaoTextView.setText(descricao);
                diagnosticoTextView.setText(diagnostico);
                quartoTextView.setText(quarto);
                dataDeAdmissaoTextView.setText(dataDeAdmissao);

            } else {
                String errorMsg = response.getString("erro_msg");
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }
    }


}
