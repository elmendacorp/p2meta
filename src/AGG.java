import java.util.HashMap;
import java.util.Random;

public class AGG {
    private HashMap<Integer, Solucion> poblacion;
    private HashMap<Integer, Solucion> poblacionGanadores;
    private Solucion mejor;
    private int tamPoblacion;
    private float time;
    private Random rd;
    private Filemanager data;
    private int semilla;
    private int evaluaciones;

    public AGG(Filemanager datos, int semilla) {
        poblacion = new HashMap<>();
        poblacionGanadores = new HashMap<>();
        tamPoblacion = 50;
        mejor = new Solucion();
        mejor.setPuntuacion(-1);
        this.semilla = semilla;
        data = datos;
        rd = new Random();
        rd.setSeed(semilla);
        evaluaciones = 0;
        inicializacion();
    }

    /**
     * Metodo para la inicializacion de la poblacion
     */
    private void inicializacion() {
        Greedy miGreedy = new Greedy(data, semilla);
        for (int i = 0; i < 50; ++i) {
            miGreedy.generaSolucion();
            poblacion.put(i, miGreedy.getSolucion());
        }
        evaluaciones = 50;
    }

    /**
     * Metodo principal del algoritmo genetico generacional con elitismo
     */
    public void ejecucion(int max) {
        time = System.nanoTime();
        double puntuacionGeneracionAnterior = 0;
        int generacionesSinMejora = 0;
        int generacion = 0;
        while (evaluaciones < max) {

            //Elitismo: si la mejor solución de la generación anterior no sobrevive, sustituye directamente la peor solución de la nueva población
            if (mejor.getPuntuacion() != -1 && !poblacion.containsValue(mejor)) {
                int peorPos = 0;
                Solucion arrayPoblacion[] = (Solucion[]) poblacion.values().toArray();
                for(int i = 0; i< poblacion.values().size(); ++i){
                    if(arrayPoblacion[i].getPuntuacion() > arrayPoblacion[peorPos].getPuntuacion()){
                        peorPos = i;
                    }
                }
                poblacion.remove(peorPos);
                poblacion.put(peorPos, mejor);
            }

            //Torneo Binario
            torneoBinario();


            //Cruce normal
            for (int i = 0; i < 18; i += 2) {

                Solucion hijo1 = new Solucion();
                hijo1.getFrecuenciasAsignadas().putAll(poblacionGanadores.get(i).getFrecuenciasAsignadas());

                Solucion hijo2 = new Solucion();
                hijo2.getFrecuenciasAsignadas().putAll(poblacionGanadores.get(i + 1).getFrecuenciasAsignadas());

                int desde = rd.nextInt(poblacionGanadores.get(0).getFrecuenciasAsignadas().size());
                int hasta = rd.nextInt(poblacionGanadores.get(0).getFrecuenciasAsignadas().size());
                if (desde > hasta) {
                    int aux = hasta;
                    hasta = desde;
                    desde = aux;
                }

                for (FrecAsignada fr : hijo1.getFrecuenciasAsignadas().values()) {
                    if (fr.getId() >= desde && fr.getId() <= hasta) {
                        hijo2.getFrecuenciasAsignadas().put(fr.getId(), new FrecAsignada(fr.getId(), fr.getFrecuencia()));
                    }
                    if (fr.getId() == hasta) break;
                }
                hijo1.calculaRestriccion(data.getRestricciones());

                for (FrecAsignada fr : hijo2.getFrecuenciasAsignadas().values()) {
                    if (fr.getId() >= desde && fr.getId() <= hasta) {
                        hijo1.getFrecuenciasAsignadas().put(fr.getId(), new FrecAsignada(fr.getId(), fr.getFrecuencia()));
                    }
                    if (fr.getId() == hasta) break;
                }
                hijo2.calculaRestriccion(data.getRestricciones());

                evaluaciones += 2;
            }

            //Mutacion

            int posMutacion = rd.nextInt(tamPoblacion);
            int posTrx = rd.nextInt(poblacionGanadores.get(posMutacion).getFrecuenciasAsignadas().size());

            int idTrx = (Integer) poblacionGanadores.get(posMutacion).getFrecuenciasAsignadas().keySet().toArray()[posTrx];

            int frRandom = rd.nextInt(data.getFrecuencias().get(data.getTransmisores().get(idTrx).getRango()).getFrecuencias().size());
            int nuevaFr = data.getFrecuencias().get(data.getTransmisores().get(idTrx).getRango()).getFrecuencias().get(frRandom);

            poblacionGanadores.get(posMutacion).getFrecuenciasAsignadas().get(idTrx).setFrecuencia(nuevaFr);

            //Buscamos la mejor solucion tras la mutacion
            mejor = calculaPosMejorsolucion(poblacionGanadores.values().toArray());

            //Reemplazamiento
            poblacion.putAll(poblacionGanadores);

        }
        time = System.nanoTime() - time;
    }

    private void torneoBinario() {
        for (int i = 0; i < poblacion.size(); ++i) {
            int posContrincante1 = rd.nextInt(poblacion.size());
            int posContrincante2 = rd.nextInt(poblacion.size());
            if (poblacion.get(posContrincante1).getPuntuacion() < poblacion.get(posContrincante2).getPuntuacion()) {
                poblacionGanadores.put(i, poblacion.get(posContrincante1));
            } else {
                poblacionGanadores.put(i, poblacion.get(posContrincante2));
            }
        }
    }

    private Solucion calculaPosMejorsolucion(Object[] poblaciones) {
        Solucion mejor = new Solucion();
        mejor.setPuntuacion(99999999);

        for (Object sol : poblaciones) {
            Solucion s = (Solucion) sol;
            if (s.getPuntuacion() < mejor.getPuntuacion()) mejor = s;
        }
        return mejor;
    }

    public void mostrarResultados(){
        System.out.println("AGG Puntuacion Mejor: " + mejor.getPuntuacion() + " Tiempo de ejecucion: " + time / 1000000 + " ms");
    }

}