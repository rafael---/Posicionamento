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
import java.util.List;

/**
 * Created by Rafael on 20/08/2016.
 */
public class Conexao extends AsyncTask<String, Void, String> {

    private StringBuilder msg;
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
        msg = new StringBuilder();

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
        } catch(Exception e)   {
            msg.append("Error: ");
            msg.append(e.getMessage());
            msg.append("\nClass: ");
            msg.append(e.getClass());
        }

        return msg.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        msg = new StringBuilder();
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

                while (aps.length() > 0) {
                    String name = aps.keys().next();
                    l.addAp(name, aps.getDouble(name));

                    aps.remove(name);
                }
                localList.add(l);
            }
        } catch(Exception e)    {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(mContext, "Sincronização finalizada!", Toast.LENGTH_SHORT).show();
    }
}
