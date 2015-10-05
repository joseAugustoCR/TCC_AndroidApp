package com.example.guto.tcc_app_v1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/*
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
*/

/**
 *  Sends the data to the server and gets the response
 */
public class SendData extends AsyncTask<Void, Void, String> {
    public AsyncResponse delegate=null;
   private String URL = "http://192.168.3.105/PhpProject2/index.php";
    private List<NameValuePair> data = new ArrayList<NameValuePair>();
    private Context context;
    private String mensagem = "";




    // Constructor to initialize the parameters
    public SendData (  List<NameValuePair> data_param, Context context_param){

        this.data = data_param;
        this.context = context_param;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    protected String doInBackground(Void... arg0) {

        if (verificaInternet(context)) {
            return sendData();
        }
        else{
            mensagem = "Sem conexão com a internet!";
            return "";
        }
    }




    private String sendData()
    {
        String response="";
        try {

            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(2000);
            conn.setConnectTimeout(2000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(data));
            writer.flush();
            writer.close();
            os.close();


            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

                throw new HttpException(responseCode+"");
            }



        } catch (java.net.SocketTimeoutException e) {
            mensagem = "Tempo de conexão expirado!";
            return null;
        }
        catch(Exception e)
        {

            mensagem = "Erro na comunicação com o servidor!";
            return null;
        }
        return response;

    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    // Função para verificar a conexão com a internet
    public static boolean verificaInternet(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }





    protected void onPostExecute(String result){
        if (mensagem.equals("")){
            delegate.sendFinish(result);    // method in the AsyncResponse interface - will be overwritten in each class
        }
        else{
            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show();
        }


    }
}

