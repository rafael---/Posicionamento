package br.edu.uffs.posicionamento;

import java.security.KeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Rafael on 20/08/2016.
 */
public class Local {
    private double coordX;
    private double coordY;
    private double distancia;
    private Map<String, Double> aps;

    public Local()  {
        aps = new HashMap<>();
    }

    public double getCoordX() {
        return coordX;
    }

    public double getCoordY() {
        return coordY;
    }

    public Map<String, Double> getAps() {
        return aps;
    }

    public void setCoordX(double coordX) {
        this.coordX = coordX;
    }

    public void setCoordY(double coordY) {
        this.coordY = coordY;
    }

    public void addAp(String apName, double level)  {
        aps.put(apName,level);
    }

    public double calcularDistancia(Local l)    {
        Map<String, Double> fpAps = l.getAps();
        double distancia = 0.;

        for(String s : MainActivity.strAps) {
            if (aps.containsKey(s) && fpAps.containsKey(s))
                distancia += Math.pow(fpAps.get(s) - aps.get(s), 2.0);
            else if(aps.containsKey(s))
                distancia += Math.pow(MainActivity.RSSI_NA - aps.get(s), 2.0);
            else if(fpAps.containsKey(s))
                distancia += Math.pow(MainActivity.RSSI_NA - fpAps.get(s), 2.0);
        }

        this.distancia = Math.sqrt(distancia);
        return this.distancia;
    }
}
