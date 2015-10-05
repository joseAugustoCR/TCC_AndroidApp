package com.example.guto.tcc_app_v1;


import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  Fragment para monitorar os sinais vitais do paciente em "tempo real"
 */
public class MonitorarFragment extends Fragment implements AsyncResponse {
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  SendData  sendData;
    private  String idPaciente;
    private  TextView temperaturaTextView;
    private  TextView taxaBatimentosTextView;
    private  TextView glicoseTextView;
    private  TextView saturacaoOxigenioTextView;
    private  TextView pressaoTextView;
    private  TextView funcaoPulmonarTextView;
    private  Timer timer;


    public MonitorarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega idPaciente do intent
        idPaciente = getActivity().getIntent().getStringExtra("idPaciente");

        // Chama método para atualizar os sinais vitais a cada 2s
        atualizaSinais();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitorar, container, false);

        temperaturaTextView = (TextView)view.findViewById(R.id.temperaturaTextView);
        taxaBatimentosTextView = (TextView)view.findViewById(R.id.taxaBatimentosTextView);
        glicoseTextView = (TextView)view.findViewById(R.id.glicoseTextView);
        pressaoTextView = (TextView)view.findViewById(R.id.pressaoTextView);
        saturacaoOxigenioTextView = (TextView)view.findViewById(R.id.saturacaoOxigenioTextView);
        funcaoPulmonarTextView = (TextView)view.findViewById(R.id.funcaoPulmonarTextView);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Caso a activity saia de foco, cancela o timer que atualiza os sinais vitais
        timer.cancel();

    }

    // Função para atualizar os sinais periodicamente
    public void atualizaSinais() {
        final Handler handler = new Handler();
         timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.d("TCC_log", "atualizando sinais");
                            // Coloca tag e idPaciente em List<NameValuePair> data
                            data.add(new BasicNameValuePair("tag", "getSinais"));
                            data.add(new BasicNameValuePair("idPaciente", idPaciente));

                            // Envia dados para o servidor
                            sendData = new SendData( data, getActivity().getApplicationContext());
                            sendData.delegate = MonitorarFragment.this;       // Método para tratar da resposta do servidor
                            sendData.execute();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 2000); // Executa a cada 2s
    }


    // Método para tratar da resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {
            JSONObject response = new JSONObject(output);         // Pega o resultado
            boolean error = response.getBoolean("erro");

            if (!error) {       // Se não houve erro
                // Decodifica a resposta
                JSONObject object = response.getJSONObject("sinais");
                String temperatura = object.getString("temperatura");
                String taxaBatimento = object.getString("taxaBatimentos");
                String glicose = object.getString("glicose");
                String pressaoSistolica = object.getString("pressaoSistolica");
                String pressaoDiastolica = object.getString("pressaoDiastolica");
                String funcaoPulmonar = object.getString("funcaoPulmonar");
                String saturacaoOxigenio = object.getString("saturacaoOxigenio");

                // Define o texto que os widgets exibem
                temperaturaTextView.setText(temperatura + " °C");
                taxaBatimentosTextView.setText(taxaBatimento+ " bpm");
                glicoseTextView.setText(glicose + " mg/dl");
                pressaoTextView.setText(pressaoSistolica + "/" + pressaoDiastolica + " mmHg");
                saturacaoOxigenioTextView.setText(saturacaoOxigenio + " %");
                funcaoPulmonarTextView.setText(funcaoPulmonar + " por minuto");

            } else {
                String errorMsg = response.getString("erro_msg");
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }


    }
}
