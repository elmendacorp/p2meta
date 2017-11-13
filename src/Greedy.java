import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Greedy {
    private float time;
    private Solucion solucionLocal;
    private Random rd;
    private Filemanager data;

    public Greedy(Filemanager datos, int semilla) {
        data = datos;
        rd = new Random();
        rd.setSeed(semilla);

    }

    public void generaSolucion() {

        time = System.nanoTime();
        solucionLocal= new Solucion();

        int randomPos = rd.nextInt(data.getTransmisores().size());
        int nodo = (Integer) data.getTransmisores().keySet().toArray()[randomPos];
        int rangoNodo = data.getTransmisores().get(nodo).getRango();
        int rangoTam = data.getFrecuencias().get(rangoNodo).getFrecuencias().size();

        int frecNodo = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(rd.nextInt(rangoTam));
        solucionLocal.getFrecuenciasAsignadas().put(nodo, new FrecAsignada(nodo, frecNodo));

        //iterar a partir del elemento aleatorio, las frecuencias se obtienen del mismo modo
        Iterator<Transmisor> miIterador;
        miIterador=data.getTransmisores().values().iterator();
        while(miIterador.hasNext()){
            Transmisor t= miIterador.next();
            if(t.getId()==nodo){
                break;
            }
        }

        while(miIterador.hasNext()) {
            Transmisor t= miIterador.next();
            HashMap<Integer, CosteFrecuencia> frecuenciasProcesadas = new HashMap<>();
            if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                int puntos = 0;
                int nuevaFrecuencia = 0;
                for (Integer fr : data.getFrecuencias().get(t.getRango()).getFrecuencias()) {
                    nuevaFrecuencia = fr;
                    puntos = calculaPuntosFrec(t.getId(), fr);
                    if (puntos == 0) {
                        break;
                    } else if (!solucionLocal.getFrecuenciasAsignadas().containsKey(fr)) {
                        frecuenciasProcesadas.put(fr, new CosteFrecuencia(fr, puntos));
                    }
                }

                if (puntos == 0) {
                    if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                        solucionLocal.getFrecuenciasAsignadas().put(t.getId(), new FrecAsignada(t.getId(), nuevaFrecuencia));
                    }
                } else {
                    int coste = 100;
                    int elegido = 0;
                    for (CosteFrecuencia crt : frecuenciasProcesadas.values()) {
                        if (crt.getCoste() < coste) {
                            coste = crt.getCoste();
                            elegido = crt.getFrecuencia();
                        }
                    }
                    if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                        solucionLocal.getFrecuenciasAsignadas().put(t.getId(), new FrecAsignada(t.getId(), elegido));
                    }
                }
            }

        }
        miIterador=data.getTransmisores().values().iterator();
        Transmisor t=miIterador.next();
        while(t.getId()!=nodo && miIterador.hasNext()) {
            HashMap<Integer, CosteFrecuencia> frecuenciasProcesadas = new HashMap<>();
            if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                int puntos = 0;
                int nuevaFrecuencia = 0;
                for (Integer fr : data.getFrecuencias().get(t.getRango()).getFrecuencias()) {
                    nuevaFrecuencia = fr;
                    puntos = calculaPuntosFrec(t.getId(), fr);
                    if (puntos == 0) {
                        break;
                    } else if (!solucionLocal.getFrecuenciasAsignadas().containsKey(fr)) {
                        frecuenciasProcesadas.put(fr, new CosteFrecuencia(fr, puntos));
                    }
                }

                if (puntos == 0) {
                    if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                        solucionLocal.getFrecuenciasAsignadas().put(t.getId(), new FrecAsignada(t.getId(), nuevaFrecuencia));
                    }
                } else {
                    int coste = 100;
                    int elegido = 0;
                    for (CosteFrecuencia crt : frecuenciasProcesadas.values()) {
                        if (crt.getCoste() < coste) {
                            coste = crt.getCoste();
                            elegido = crt.getFrecuencia();
                        }
                    }
                    if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                        solucionLocal.getFrecuenciasAsignadas().put(t.getId(), new FrecAsignada(t.getId(), elegido));
                    }
                }
            }
            t=miIterador.next();
        }
        solucionLocal.calculaRestriccion(data.getRestricciones());
        time = System.nanoTime() - time;
    }

    public int calculaPuntosFrec(int posicion, int frecuencia) {
        int puntosOriginal = 0;
        for (Restriccion rs : data.getRestricciones().get(posicion)) {
            if (solucionLocal.getFrecuenciasAsignadas().containsKey(rs.getId_restriccion())) {
                int frecuenciaRestringida = solucionLocal.getFrecuenciasAsignadas().get(rs.getId_restriccion()).getFrecuencia();
                if (Math.abs(frecuencia - frecuenciaRestringida) <= rs.getTolerancia()) {
                    puntosOriginal += rs.getPenalizacion();
                }
            }

        }
        return puntosOriginal;
    }


    public float getTime() {
        return time / 1000000;
    }

    public Solucion getSolucion(){return solucionLocal;}

    /**
     * Funcion para mostrar los resultados
     */
    public void getResultados() {

        System.out.println(solucionLocal.getPuntuacion() + " " + time / 1000000 + " ms");
        for (FrecAsignada fr : solucionLocal.getFrecuenciasAsignadas().values()) {
            //System.out.println(fr.getId()+"\t"+fr.getFrecuencia());
        }
    }

}

