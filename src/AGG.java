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
        mejor.setPuntuacion(99999999);
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
        poblacion.clear();
        Greedy miGreedy = new Greedy(data, semilla);
        for (int i = 0; i < 50; ++i) {
            miGreedy.generaSolucion();
            poblacion.put(i, miGreedy.getSolucion());
        }
        evaluaciones += 50;
    }

    /**
     * Metodo principal del algoritmo genetico generacional con elitismo
     */
    public void ejecucion(int max) {
        time = System.nanoTime();
        int generacionesSinMejora = 0;
        int generacion = 0;

        while (evaluaciones < max) {

            //Elitismo: si la mejor solución de la generación anterior no sobrevive, sustituye directamente la peor solución de la nueva población
            if (!poblacion.containsValue(mejor)) {
                int posicionPeor = 0;
                for (int i = 0; i < poblacion.values().size(); ++i) {
                    if (poblacion.get(i).getPuntuacion() > poblacion.get(posicionPeor).getPuntuacion()) {
                        posicionPeor = i;
                    }
                }
                poblacion.remove(posicionPeor);
                poblacion.put(posicionPeor, mejor);
            }

            //Torneo Binario
            torneoBinario();

            //Cruce normal
            for (int i = 0; i < 18; ++i) {

                int desde = rd.nextInt(poblacionGanadores.get(i).getFrecuenciasAsignadas().size());
                int hasta = rd.nextInt(poblacionGanadores.get(i).getFrecuenciasAsignadas().size());
                if (desde > hasta) {
                    int aux = hasta;
                    hasta = desde;
                    desde = aux;
                }

                int posPadre = rd.nextInt(poblacionGanadores.size());
                int posMadre = rd.nextInt(poblacionGanadores.size());
                while (posMadre == posPadre) {
                    posMadre = rd.nextInt(poblacionGanadores.size());
                }

                Solucion padre = poblacionGanadores.get(posPadre);
                Solucion madre = poblacionGanadores.get(posMadre);
                Solucion hijo1 = new Solucion();
                Solucion hijo2 = new Solucion();

                for (FrecAsignada f : padre.getFrecuenciasAsignadas().values()) {
                    if (f.getId() >= desde && f.getId() <= hasta) {
                        hijo1.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                        hijo2.getFrecuenciasAsignadas().put(f.getId(), f);
                    } else {
                        hijo1.getFrecuenciasAsignadas().put(f.getId(), f);
                        hijo2.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                    }
                }
                evaluaciones += 2;
            }

            //Mutacion
            int posMutacion = rd.nextInt(tamPoblacion);
            mutacion(poblacionGanadores.get(posMutacion));

            //Buscamos la mejor solucion tras la mutacion
            Solucion posibleMejor = calculaMejorsolucion(poblacionGanadores.values().toArray());

            //Si no hemos mejorado
            if (posibleMejor.getPuntuacion() < mejor.getPuntuacion()) {
                mejor = posibleMejor;
            } else {
                ++generacionesSinMejora;
            }

            if (generacionesSinMejora == 20) {
                inicializacion();
                generacionesSinMejora = 0;
            } else {
                //Reemplazamiento
                poblacion.clear();
                poblacion.putAll(poblacionGanadores);
            }
            ++generacion;

            //System.out.println("Generacion: " + generacion + " .Mejor solucion: " + mejor.getPuntuacion() + ". Evaluaciones " + evaluaciones);

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

    private Solucion calculaMejorsolucion(Object[] poblaciones) {
        Solucion mejor = new Solucion();
        mejor.setPuntuacion(99999999);

        for (Object sol : poblaciones) {
            Solucion s = (Solucion) sol;
            if (s.getPuntuacion() < mejor.getPuntuacion()) mejor = s;
        }
        return mejor;
    }

    public void mostrarResultados() {
        System.out.println("AGG Puntuacion Mejor: " + mejor.getPuntuacion() + " Tiempo de ejecucion: " + time / 1000000 + " ms");
    }

    public void mutacion(Solucion hijo) {
        for (FrecAsignada f : hijo.getFrecuenciasAsignadas().values()) {
            if (rd.nextDouble() < 0.1) {
                int rangoNodo = data.getTransmisores().get(f.getId()).getRango();
                int rangoTam = data.getFrecuencias().get(rangoNodo).getFrecuencias().size();
                int frecNodo = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(rd.nextInt(rangoTam));
                f.setFrecuencia(frecNodo);

            }
        }
    }
}