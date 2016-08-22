package br.edu.uffs.posicionamento;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private List<Local> locais;

    private int qtdLocais;
    private double erroAcumuladoKNN;
    private double erroAcumuladoKWNN;
    private Conexao conexao;
    private Spinner dropdown;
    DecimalFormat decimalFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qtdLocais = 0;
        erroAcumuladoKWNN = 0.;
        erroAcumuladoKNN = 0.;

        dropdown = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{"2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        decimalFormat = (DecimalFormat)nf;
        decimalFormat.applyPattern("####.##");
    }

    class Comparador implements Comparator<Local>  {
        @Override
        public int compare(Local lhs, Local rhs) {
            if(lhs.getDistancia() > rhs.getDistancia())
                return -1;
            if(lhs.getDistancia() == rhs.getDistancia())
                return 0;
            return 1;
        }
    }

    public void obterPosicao(View v)    {
        if(locais == null || conexao == null)  {
            Toast.makeText(getApplicationContext(), "Por favor, sincronize o aplicativo com o servidor", Toast.LENGTH_SHORT).show();
            return;
        }
        if(conexao.getStatus() != AsyncTask.Status.FINISHED)    {
            Toast.makeText(getApplicationContext(), "Por favor, aguarde a sincronização com o servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);

        Local localAtual = new Local();

        List<ScanResult> listAPs = wifiMgr.getScanResults();

        for(ScanResult sc : listAPs)
            localAtual.addAp(sc.BSSID, sc.level);

        String msg = "";

        for(Local l : locais)   {
            double x = l.getCoordX();
            double y = l.getCoordY();
            double distancia = localAtual.calcularDistancia(l);

            msg += "("+x+", "+y+") -> " + distancia + "\n";
        }

        Comparador comparador = new Comparador();
        Collections.sort(locais, comparador);

        int K = Integer.valueOf(dropdown.getSelectedItem().toString());

        Pair<Double, Double> knnDist = kNN(K);
        Pair<Double, Double> kwnnDist = kWNN(K);

        TextView txt1 = (TextView) findViewById(R.id.textView);
        TextView txt2 = (TextView) findViewById(R.id.textView2);

        String txt2NN = "Posição KNN (" + decimalFormat.format(knnDist.first) + ", " + decimalFormat.format(knnDist.second) + ")";
        String txt2WNN = "Posição KWNN (" + decimalFormat.format(kwnnDist.first) + ", " + decimalFormat.format(kwnnDist.second) + ")";

        txt1.setText(txt2NN);
        txt2.setText(txt2WNN);

        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    public Pair<Double,Double> kNN(final int K)  {
        double x = 0., y = 0.;
        for(int i = 0; i < K; i++)  {
            x += locais.get(i).getCoordX() / (double)K;
            y += locais.get(i).getCoordY() / (double)K;
        }
        return new Pair<>(x,y);
    }

    public Pair<Double, Double> kWNN(final int K)   {
        double x = 0., y = 0.;
        final double EPS = 10e-4;
        for(int i = 0; i < K; i++)  {
            x += 1./(locais.get(i).getDistancia() + EPS) * locais.get(i).getCoordX();
            y += 1./(locais.get(i).getDistancia() + EPS) * locais.get(i).getCoordY();
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

    public void melhorarLocalizacao(View v) {
        TextView txtLocalKNN = (TextView) findViewById(R.id.textView2);
        TextView txtLocalKWNN = (TextView) findViewById(R.id.textView);

        EditText localCorretoX = (EditText) findViewById(R.id.editText);
        EditText localCorretoY = (EditText) findViewById(R.id.editText2);

        String []localKNN = txtLocalKNN.getText().toString().split("[(),]");
        String []localKWNN = txtLocalKWNN.getText().toString().split("[(),]");

        try {
            double xKNN = Double.valueOf(localKNN[1]);
            double yKNN = Double.valueOf(localKNN[2]);

            double xKWNN = Double.valueOf(localKWNN[1]);
            double yKWNN = Double.valueOf(localKWNN[2]);

            double xCorreto = Double.valueOf(localCorretoX.getText().toString());
            double yCorreto = Double.valueOf(localCorretoY.getText().toString());


            this.qtdLocais++;
            this.erroAcumuladoKNN += Math.abs(xKNN - xCorreto) + Math.abs(yKNN - yCorreto);
            this.erroAcumuladoKWNN += Math.abs(xKWNN - xCorreto) + Math.abs(yKWNN - yCorreto);

            TextView txtMsgKNN = (TextView) findViewById(R.id.textView6);
            TextView txtMsgKWNN = (TextView) findViewById(R.id.textView5);


            String msgKNN = "Erro médio KNN: " + decimalFormat.format(erroAcumuladoKNN/(double)qtdLocais) + " m";
            String msgKWNN = "Erro médio KWNN: " + decimalFormat.format(erroAcumuladoKWNN/(double)qtdLocais) + " m";

            txtMsgKNN.setText(msgKNN);
            txtMsgKWNN.setText(msgKWNN);
        } catch (Exception e)   {
            Toast.makeText(getApplicationContext(), "Preencha corretamente as coordenadas", Toast.LENGTH_SHORT).show();
        }
    }
}
