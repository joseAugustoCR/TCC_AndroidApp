package com.example.guto.tcc_app_v1;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para visualizar o histórico do paciente
 */
public class HistoricoFragment extends Fragment implements AsyncResponse {
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  SendData  sendData;
    private  String idPaciente;
    private  CheckBox fumanteCheckBox;
    private  CheckBox usoDeDrogaCheckBox;
    private  CheckBox usoDeAlcoolCheckBox;
    private  TextView alergiaTextView;
    private  TextView infoAdicionalTextView;


    public HistoricoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega idPaciente do intent
        idPaciente = getActivity().getIntent().getStringExtra("idPaciente");

        // Coloca tag e idPaciente em List<NameValuePair> data
        data.add(new BasicNameValuePair("tag", "getHistorico"));
        data.add(new BasicNameValuePair("idPaciente", idPaciente));

        // Envia os dados para o servidor
        sendData = new SendData(data, getActivity().getApplicationContext());
        sendData.delegate = HistoricoFragment.this;       // Método para tratar a resposta do servidor
        sendData.execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        fumanteCheckBox = (CheckBox)view.findViewById(R.id.fumanteCheckBox);
       usoDeAlcoolCheckBox =  (CheckBox)view.findViewById(R.id.usoDeAlcoolCheckBox);
        usoDeDrogaCheckBox =  (CheckBox)view.findViewById(R.id.usoDeDrogaCheckBox);
        alergiaTextView =  (TextView)view.findViewById(R.id.alergiasTextView);
        infoAdicionalTextView =  (TextView)view.findViewById(R.id.infoAdicionalTextView);

        return view;
    }


    // Método para tratar a resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
            boolean error = response.getBoolean("erro");

            if (!error) {
                // Decodifica a resposta
                JSONObject object = response.getJSONObject("historico");
                String fumante = object.getString("fumante");
                String usoDeAlcool = object.getString("usoDeAlcool");
                String usoDeDroga = object.getString("usoDeDroga");
                String alergia = object.getString("alergia");
                String infoAdicional = object.getString("infoAdicional");

                // Configura os checkboxs de acordo com a resposta
                if (fumante.equals("1")){
                    fumanteCheckBox.setChecked(true);
                }
                if (usoDeAlcool.equals("1")){
                    usoDeAlcoolCheckBox.setChecked(true);
                }
                if (usoDeDroga.equals("1")){
                    usoDeDrogaCheckBox.setChecked(true);
                }

                // Define o texto dos campos
                alergiaTextView.setText(alergia);
                infoAdicionalTextView.setText(infoAdicional);

            } else {
                String errorMsg = response.getString("erro_msg");
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }
    }


}
