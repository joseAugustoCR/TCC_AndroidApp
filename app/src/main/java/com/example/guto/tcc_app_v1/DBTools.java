package com.example.guto.tcc_app_v1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Classe para gerenciar SQLite DB
 */
public class DBTools extends SQLiteOpenHelper implements AsyncResponse{
    private  SendData  sendData;
    private  List<NameValuePair> data = new ArrayList<NameValuePair>();
    private Context context;



    public DBTools(Context applicationContext){
        super(applicationContext, "mySQLiteDB", null,1);
        this.context = applicationContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Cria tabelas
        String query = "CREATE TABLE Medicamento ( idMedicamento INTEGER PRIMARY KEY, nome TEXT) ";
        db.execSQL(query);
         query = "CREATE TABLE CID ( idCID INTEGER PRIMARY KEY, nome TEXT) ";
        db.execSQL(query);


        // Requisição ao servidor para obter diagnosticos
        data.add(new BasicNameValuePair("tag", "getDiagnosticos"));

        // Sends data to server
        sendData = new SendData(data, context);
        sendData.delegate = DBTools.this;       // override method when finishes
        sendData.execute();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onUpgrade desconsidera tabelas já existentes
        db.execSQL("DROP TABLE IF EXISTS " + "Medicamento");
        db.execSQL("DROP TABLE IF EXISTS " + "CID");
        // Cria tabelas novas
        onCreate(db);
    }

    // Atualiza banco de dados
    public void updateDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + "Medicamento");
        db.execSQL("DROP TABLE IF EXISTS " + "CID");
        // Cria tabelas novas
        onCreate(db);

    }

    // Insere um medicamento a tabela de Medicamento
    public void insereMedicamento(HashMap<String, String> queryValues){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("idMedicamento", queryValues.get("idMedicamento"));
        values.put("nome", queryValues.get("nome"));

        db.insert("Medicamento", null, values);
        db.close();
    }


    // Insere CID a tabela
    public void insereCID(HashMap<String, String> queryValues){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("idCID", queryValues.get("idCID"));
        values.put("nome", queryValues.get("nome"));

        db.insert("CID", null, values);
        db.close();
    }


    // Obtem todos os medicamentos armazenados
    public  List<SpinnerItem>  getMedicamentos(){

        List<SpinnerItem> medicamentosList = new ArrayList<SpinnerItem>();

        String query = "SELECT * FROM Medicamento";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {

            do {
                SpinnerItem item = new SpinnerItem();           // Create an item type
                item.id = cursor.getString(0);
                item.nome = cursor.getString(1);

                medicamentosList.add(item);        // Add to list

            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        return  medicamentosList;
    }


    // Obtém todas as doenças armazenadas na tabela CID
    public  List<SpinnerItem> getCID(){

        List<SpinnerItem> diagnosticosList = new ArrayList<SpinnerItem>();

        String query = "SELECT * FROM CID";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {

            do {
                SpinnerItem item = new SpinnerItem();           // Create an item type
                item.id = cursor.getString(0);
                item.nome = cursor.getString(1);

                diagnosticosList.add(item);        // Add to list

            } while (cursor.moveToNext());
        }
        return  diagnosticosList;
    }


    // Trata a resposta do servidor
    @Override
    public void sendFinish(String output) {
        try {

            JSONObject response = new JSONObject(output);         // Pega o resultado
            String tag = response.getString("tag");
            boolean error = response.getBoolean("erro");

            if (tag.equals("getDiagnosticos"))    // Resposta da solicitação de diagnosticos
            {
                if (!error) {    // Sem erro
                    JSONObject diagnosticos = response.getJSONObject("diagnosticos");

                    Iterator<String> iter = diagnosticos.keys();
                    String key;
                    JSONObject diagnostico;
                    HashMap<String, String> queryValuesMap;

                    while (iter.hasNext()) {                                                // Iteração entre os diagnosticos
                        key = iter.next();                                                        // Pega o próximo diagnóstico
                        try {
                            diagnostico = diagnosticos.getJSONObject(key);

                            // Coloca os valores no HashMao
                            queryValuesMap = new HashMap<String, String>();
                            queryValuesMap.put("nome",  diagnostico.getString("nome"));
                            queryValuesMap.put("idCID", diagnostico.getString("idCID"));

                            // Salva no DB
                            this.insereCID(queryValuesMap);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Requisição ao servidor para obter medicamentos
                    data.add(new BasicNameValuePair("tag", "getMedicamentos"));

                    // Envia para o servidor
                    sendData = new SendData( data, context);
                    sendData.delegate = DBTools.this;       // define o método que receberá a resposta
                    sendData.execute();

                }

            } else if (tag.equals("getMedicamentos")) {    // Resposta do salvamento de uma requisicao

                if (!error) {    // Sem erro
                    JSONObject medicamentos = response.getJSONObject("medicamentos");

                    Iterator<String> iter = medicamentos.keys();

                    String key;
                    JSONObject medicamento;

                    HashMap<String, String> queryValuesMap;

                    while (iter.hasNext()) {                                                // Iteração entre medicamentos
                        key = iter.next();                                           // Obtem próximo medicamento
                        try {
                            medicamento = medicamentos.getJSONObject(key);

                            // Coloca valores em HashMap
                            queryValuesMap = new HashMap<String, String>();
                            queryValuesMap.put("nome", medicamento.getString("nome"));
                            queryValuesMap.put("idMedicamento", medicamento.getString("idMedicamento"));

                            // Armazena
                            this.insereMedicamento(queryValuesMap);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        } catch (JSONException e) {
            Log.e("log_tag", "Error parsin data " + e.toString());
        }


    }

}
