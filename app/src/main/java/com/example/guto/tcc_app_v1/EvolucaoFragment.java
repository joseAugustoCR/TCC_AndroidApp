package com.example.guto.tcc_app_v1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Fragment para registrar uma evolução
 */
public class EvolucaoFragment extends Fragment implements AsyncResponse{
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  SendData  sendData;
    private  EditText descricaoEditText;
    private  Button registraEvolucaoButton;
    private  String idPaciente;
    private DrawerLayout mDrawerLayout;



    public EvolucaoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_evolucao, container, false);

        descricaoEditText = (EditText)view.findViewById(R.id.descricaoEditText);
        registraEvolucaoButton = (Button)view.findViewById(R.id.registraEvolucaoButton);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        // Clique do botão registrar
        registraEvolucaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String descricao = descricaoEditText.getText().toString();


                if (descricao.isEmpty()){
                    Toast.makeText(getActivity(), "Insira uma descrição", Toast.LENGTH_SHORT).show();
                }
                else{

                    // Coloca os dados na Lista
                    data.add(new BasicNameValuePair("tag", "registraEvolucao"));
                    data.add(new BasicNameValuePair("descricao", descricao));
                    data.add(new BasicNameValuePair("idPaciente", idPaciente));

                    // Envia para o servidor
                    sendData = new SendData(  data, getActivity().getApplicationContext());
                    sendData.delegate = EvolucaoFragment.this;       // Método para pegar resposta do servidor
                    sendData.execute();
                }
            }
        });


        return view;
    }


    // Método para pegar resposta do serevidor
    @Override
    public void sendFinish(String output) {
        try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
            boolean error = response.getBoolean("erro");

            if (!error) {       // Salvou corretamente
                Toast.makeText(getActivity(), "Evolução Registrada!", Toast.LENGTH_LONG).show();
                mDrawerLayout.openDrawer(Gravity.LEFT);

                // Limpa campos
                descricaoEditText.setText("");

            } else {
                String errorMsg = response.getString("erro_msg");
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }
    }


}
