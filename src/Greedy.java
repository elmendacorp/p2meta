import java.util.HashMap;
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
        solucionLocal = new Solucion();
    }

    public void generaSolucion() {
        time = System.nanoTime();
        HashMap<Integer, CosteFrecuencia> frecuenciasProcesadas = new HashMap<>();

        int rdn = rd.nextInt(data.getTransmisores().size());
        int nodo = (Integer) data.getTransmisores().keySet().toArray()[rdn];
        int frecNodo = data.getTransmisores().get(nodo).getRango();
        frecNodo = data.getFrecuencias().get(frecNodo).getFrecuencias().get(rd.nextInt(data.getFrecuencias().get(frecNodo).getFrecuencias().size()));
        solucionLocal.getFrecuenciasAsignadas().put(nodo, new FrecAsignada(nodo, frecNodo));


        for (Transmisor t : data.getTransmisores().values()) {
            if (!solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                int puntos = 0;
                int nuevaFrecuencia = 0;
                for (Integer fr : data.getFrecuencias().get(t.getRango()).getFrecuencias()) {
                    nuevaFrecuencia = fr;
                    puntos = puntosFrecuencia(t.getId(), fr);
                    if (puntos == 0) {
                        break;
                    } else if (!solucionLocal.getFrecuenciasAsignadas().containsKey(fr)) {
                        frecuenciasProcesadas.put(fr, new CosteFrecuencia(fr, puntos));
                    }
                    puntos = puntosFrecuencia(t.getId(), fr);

                }

                if (puntos == 0) {
                    if (solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                    } else {
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
                    if (solucionLocal.getFrecuenciasAsignadas().containsKey(t.getId())) {
                    } else {
                        solucionLocal.getFrecuenciasAsignadas().put(t.getId(), new FrecAsignada(t.getId(), elegido));
                    }
                }
            }

        }

        solucionLocal.calculaRestriccion(data.getRestricciones());
        time = System.nanoTime() - time;
    }

    public int puntosFrecuencia(int posicion, int frecuencia) {
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

    public Solucion getSolucion() {
        return solucionLocal;
    }

    public float getTime() {
        return time / 1000000;
    }

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

