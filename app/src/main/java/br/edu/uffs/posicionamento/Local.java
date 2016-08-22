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
    private int coordX;
    private int coordY;
    private double distancia;
    private Map<String, Integer> aps;

    public Local()  {
        aps = new HashMap<>();
    }

    public int getCoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public double getDistancia() {
        return distancia;
    }

    public Map<String, Integer> getAps() {
        return aps;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    public void addAp(String apName, int level)  {
        aps.put(apName,level);
    }

    public double calcularDistancia(Local l)    {
        Map<String,Integer> fpAps = l.getAps();
        double distancia = 0.;

        for(String s : fpAps.keySet())
            if (aps.containsKey(s))
                distancia += Math.pow((double) (fpAps.get(s) - aps.get(s)), 2.0);

        fpAps.keySet().removeAll(aps.keySet());

        for(String s : fpAps.keySet())
            distancia += Math.pow(-127.0 - (double)fpAps.get(s), 2.0);

        Set<String> temp = new HashSet<>(aps.keySet());

        temp.removeAll(fpAps.keySet());

        for(String s : temp)
            distancia += Math.pow(-127.0 - (double)aps.get(s), 2.0);

        this.distancia = Math.sqrt(distancia);
        return this.distancia;
    }
}
