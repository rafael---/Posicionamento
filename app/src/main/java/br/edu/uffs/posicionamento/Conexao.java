package br.edu.uffs.posicionamento;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Rafael on 20/08/2016.
 */
public class Conexao extends AsyncTask<String, Integer, String> {
    private Context mContext;
    private List<Local> localList;

    public Conexao(Context context, List<Local> locais) {
        mContext = context;
        localList = locais;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection httpURLConnection;
        StringBuilder msg = new StringBuilder();

        try {
            url = new URL(params[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String serverMsg;

            while((serverMsg = bufferedReader.readLine()) != null)    {
                msg.append(serverMsg);
            }

            bufferedReader.close();
            httpURLConnection.disconnect();
        } catch(Exception e) {
            msg.append("Error: ");
            msg.append(e.getMessage());
            msg.append("\nClass: ");
            msg.append(e.getClass());
        }

        String s = msg.toString();

        if(s.contains("Error"))
            return s;

        try {
            JSONObject json = new JSONObject(s);
            JSONArray jsonArray = json.getJSONArray("data");
            final int tam = jsonArray.length();

            for (int i = 0; i < tam; i++) {
                Local l = new Local();
                JSONObject coord = jsonArray.getJSONObject(i);

                l.setCoordX(coord.getDouble("x"));
                l.setCoordY(coord.getDouble("y"));

                JSONObject aps = coord.getJSONObject("ap");

                for(String strAp : MainActivity.strAps)
                    if(aps.has(strAp))
                        l.addAp(strAp, aps.getDouble(strAp));

                localList.add(l);
            }
        } catch(Exception e)    {
            msg.append('\n');
            msg.append("Error in JSON Object");
        }
        return msg.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        Toast.makeText(mContext, (s.contains("Error")? s :"Sincronização finalizada!"), Toast.LENGTH_SHORT).show();
    }
}
