package br.edu.uffs.posicionamento;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rafael on 05/09/16.
 */
public class Sincronizacao extends AsyncTask<String, Integer, Void> {

    private Boolean mError;
    private String serverResponse;
    private Context mContext;

    public Sincronizacao(Context context)   {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        URL url;
        HttpURLConnection httpURLConnection;

        String jsonData = params[1];
        serverResponse = "";
        mError = false;

        try {
            url = new URL(params[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
            bufferedWriter.write(jsonData);
            bufferedWriter.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String temp = "";
            while((temp = bufferedReader.readLine()) != null)
                serverResponse += temp;

            bufferedReader.close();

            httpURLConnection.disconnect();
        } catch(Exception e) {
            mError = true;
            serverResponse = e.getMessage() + "\n" + e.getClass().toString();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(mContext, serverResponse, Toast.LENGTH_SHORT).show();
    }
}
