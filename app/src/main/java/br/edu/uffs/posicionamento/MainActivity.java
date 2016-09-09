package br.edu.uffs.posicionamento;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static String[] strAps = {"44:ad:d9:e5:36:d0", "0c:27:24:8e:bb:40", "44:ad:d9:e5:5f:40"};
    public final static int RSSI_NA = -95;

    private List<Local> locais;

    private Conexao conexao;
    DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        decimalFormat = (DecimalFormat)nf;
        decimalFormat.applyPattern("####.##");
    }

    class Comparador implements Comparator<Local>  {
        private Local localBase;

        Comparador(Local base)  {
            this.localBase = base;
        }

        @Override
        public int compare(Local lhs, Local rhs) {
            double a = localBase.calcularDistancia(lhs), b = localBase.calcularDistancia(rhs);
            return (a > b)? 1 : (a == b)? 0 : -1;
        }
    }

    public void obterPosicao(View v)   {
        if(locais == null || conexao == null)  {
            Toast.makeText(getApplicationContext(), "Por favor, sincronize o aplicativo com o servidor", Toast.LENGTH_SHORT).show();
            return;
        }
        if(conexao.getStatus() != AsyncTask.Status.FINISHED)    {
            Toast.makeText(getApplicationContext(), "Por favor, aguarde a sincronização com o servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText localCorretoX = (EditText) findViewById(R.id.editText);
        EditText localCorretoY = (EditText) findViewById(R.id.editText2);

        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);

        Local localAtual = new Local();

        List<ScanResult> listAPs = wifiMgr.getScanResults();

        for(ScanResult sc : listAPs)
            for(String s : strAps)
                if(sc.BSSID.equals(s))
                    localAtual.addAp(sc.BSSID, sc.level);

        Comparador comparador = new Comparador(localAtual);
        Collections.sort(locais, comparador);

        String strSync = "data=";

        try {
            for (Local l : locais) {
                JSONObject json = new JSONObject();

                json.put("x", l.getCoordX());
                json.put("y", l.getCoordY());
                json.put("distancia", localAtual.calcularDistancia(l));

                try {
                    float cx = Float.valueOf(localCorretoX.getText().toString());
                    float cy = Float.valueOf(localCorretoY.getText().toString());
                    json.put("correct_x", cx);
                    json.put("correct_y", cy);

                    Map<String, Double> aps = l.getAps();
                    for(int i = 0, len = strAps.length; i < len; i++)   {
                        String apName = String.format(Locale.ENGLISH, "ap%d",i+1);
                        if(aps.containsKey(strAps[i]))
                            json.put(apName, aps.get(strAps[i]));
                        else
                            json.put(apName, RSSI_NA);
                    }
                } catch(NumberFormatException ignored)    {}

                strSync += "$" + json.toString();
            }

        } catch (JSONException e)   {
            Toast.makeText(getApplicationContext(),"Erro ao criar objeto JSON",Toast.LENGTH_LONG).show();
        }

        try {
            Sincronizacao sincronizacao = new Sincronizacao(getApplicationContext());
            sincronizacao.execute("http://dadosuffscco.site88.net/sync_measures.php",strSync);
        } catch (NullPointerException e)    {
            Toast.makeText(getApplicationContext(),"Objeto Nulo",Toast.LENGTH_LONG).show();
        }

        Pair<Double, Double> knnDist3 = kNN(3);
        Pair<Double, Double> kwnnDist3 = kWNN(localAtual, 3);
        Pair<Double, Double> knnDist4 = kNN(4);
        Pair<Double, Double> kwnnDist4 = kWNN(localAtual, 4);

        TextView txt3NN = (TextView) findViewById(R.id.textView);
        TextView txt3WNN = (TextView) findViewById(R.id.textView2);
        TextView txt4NN = (TextView) findViewById(R.id.textView5);
        TextView txt4WNN = (TextView) findViewById(R.id.textView6);

        String str3NN = "Posição 3NN (" + decimalFormat.format(knnDist3.first) + ", " + decimalFormat.format(knnDist3.second) + ")";
        String str3WNN = "Posição 3WNN (" + decimalFormat.format(kwnnDist3.first) + ", " + decimalFormat.format(kwnnDist3.second) + ")";

        String str4NN = "Posição 4NN (" + decimalFormat.format(knnDist4.first) + ", " + decimalFormat.format(knnDist4.second) + ")";
        String str4WNN = "Posição 4WNN (" + decimalFormat.format(kwnnDist4.first) + ", " + decimalFormat.format(kwnnDist4.second) + ")";

        txt3NN.setText(str3NN);
        txt3WNN.setText(str3WNN);
        txt4NN.setText(str4NN);
        txt4WNN.setText(str4WNN);
    }

    public Pair<Double,Double> kNN(final int K)  {
        double x = 0., y = 0.;
        for(int i = 0; i < K; i++)  {
            x += locais.get(i).getCoordX() / (double)K;
            y += locais.get(i).getCoordY() / (double)K;
        }
        return new Pair<>(x,y);
    }

    public Pair<Double, Double> kWNN(Local localAtual, final int K)   {
        double x = 0., y = 0.;
        final double EPS = 10e-4;
        for(int i = 0; i < K; i++)  {
            Local temp = locais.get(i);
            x += 1./(localAtual.calcularDistancia(temp) + EPS) * temp.getCoordX();
            y += 1./(localAtual.calcularDistancia(temp) + EPS) * temp.getCoordY();
        }
        return new Pair<>(x,y);
    }

    public void obterFingerprint(View v)    {
        if(locais != null)
            return;

        locais = new ArrayList<>();

        conexao = new Conexao(getApplicationContext(), locais);
        conexao.execute("http://dadosuffscco.site88.net/list_local.php");

        Button btnFingerprint = (Button) findViewById(R.id.button2);
        btnFingerprint.setVisibility(View.INVISIBLE);
    }
}
