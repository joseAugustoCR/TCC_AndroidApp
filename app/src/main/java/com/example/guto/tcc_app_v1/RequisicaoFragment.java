package com.example.guto.tcc_app_v1;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 *  Fragment para registrar uma requisição
 */
public class RequisicaoFragment extends Fragment implements AsyncResponse {
    private  Spinner medicamentosSpinner;
    private  Button registraRequisicaoButton;
    private  EditText doseEditText;
    private  EditText descricaoEditText;
    private  SendData  sendData;
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private  String idMedico;
    private  String idPaciente;
    private DrawerLayout mDrawerLayout;


    public RequisicaoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pega os dados passados por intent
        idPaciente = getActivity().getIntent().getStringExtra("idPaciente");
        idMedico = getActivity().getIntent().getStringExtra("idMedico");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requisicao, container, false);

        medicamentosSpinner = (Spinner)view.findViewById(R.id.medicamentosSpinner);
        registraRequisicaoButton = (Button) view.findViewById(R.id.registraRequisicaoButton);
        doseEditText = (EditText) view.findViewById(R.id.doseEditText);
        descricaoEditText = (EditText) view.findViewById(R.id.descricaoRequisicaoEditText);

        medicamentosSpinner.setFocusable(true);
        medicamentosSpinner.setFocusableInTouchMode(true);


        DBTools dbTools = new DBTools(getActivity());

        // Pega os medicamentos salvos no SQLite
       List <SpinnerItem>  medicamentosList = dbTools.getMedicamentos();

        // Hint
        SpinnerItem hint = new SpinnerItem();           // Create an item type
        hint.nome = " Selecione um medicamento...";
        hint.id = "NULL";
        medicamentosList.add(hint);

        // Preenche spinner com os medicamentos da lista
        ArrayAdapter<SpinnerItem> arrayAdapter = new ArrayAdapter<SpinnerItem>(getActivity(), android.R.layout.simple_list_item_1, medicamentosList);   // Populate the ListView with all patients
        arrayAdapter.sort(new Comparator<SpinnerItem>() {
            @Override
            public int compare(SpinnerItem lhs, SpinnerItem rhs) {
                return lhs.nome.compareTo(rhs.nome);
            }
        });

        medicamentosSpinner.setAdapter(arrayAdapter);

        // Campo para inserir a dose só fica visível se selecionar um medicamento
        doseEditText.setVisibility(View.GONE);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);

        // Seleção do spinner muda a visibilidade do campo da dose
        medicamentosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){
                    doseEditText.setVisibility(View.VISIBLE);
                }else{
                    doseEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Clique do botao de registrar
        registraRequisicaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpinnerItem item = (SpinnerItem)medicamentosSpinner.getSelectedItem();

                // Pega medicamento selecionado
                String idMedicamento = item.id;
                String dose = doseEditText.getText().toString();
                String descricao = descricaoEditText.getText().toString();


                Log.d("log_tag", "id medicamento = " + idMedicamento + " descricao= "+descricao);

                // Dados para enviar List<NameValuePair> data
                data.add(new BasicNameValuePair("tag", "registraRequisicao"));
                data.add(new BasicNameValuePair("idMedico", idMedico));
                data.add(new BasicNameValuePair("idPaciente", idPaciente));
                data.add(new BasicNameValuePair("idMedicamento", idMedicamento));
                data.add(new BasicNameValuePair("doseAdministrada", dose));
                data.add(new BasicNameValuePair("descricao", descricao));


                // Envia para o servidor
                sendData = new SendData( data, getActivity().getApplicationContext());
                sendData.delegate = RequisicaoFragment.this;       // Método para tratar da resposta do servidors
                sendData.execute();

            }
        });

        return view;
    }


    // Método para tratar da resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {


            JSONObject response = new JSONObject(output);         // Pega o resultado

            String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");



            if (tag.equals("registraRequisicao")) {    // Resposta do salvamento de uma requisicao


                    if (!error){
                        Toast.makeText(getActivity(), "Requisição Registrada!", Toast.LENGTH_LONG).show();
                        mDrawerLayout.openDrawer(Gravity.LEFT); // Abre menu lateral

                        // Limpa campos dos widgets
                        medicamentosSpinner.setSelection(0);
                        doseEditText.setText("");
                        descricaoEditText.setText("");

                    }else{
                        Log.e("log_tag", "Erro ao registrar requisicao");
                        String errorMsg = response.getString("erro_msg");
                        Toast.makeText(getActivity(),  errorMsg, Toast.LENGTH_LONG).show();
                    }
            }

        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }

    }


}
