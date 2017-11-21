import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class AGG {
    private HashMap<Integer, Solucion> poblacion;
    private HashMap<Integer, Solucion> poblacionGanadores;
    private Solucion mejor;
    private Greedy miGreedy;
    private boolean blx;
    private float time;
    private Random rd;
    private Filemanager data;
    private int semilla;
    private int evaluaciones;

    public AGG(Filemanager datos, int semilla, boolean usarBLX) {
        poblacion = new HashMap<>();
        poblacionGanadores = new HashMap<>();
        mejor = new Solucion();
        mejor.setPuntuacion(999999);
        blx = usarBLX;
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
        miGreedy = new Greedy(data, semilla);
        for (int i = 0; i < 50; ++i) {
            miGreedy.generaSolucion();
            poblacion.put(i, new Solucion(miGreedy.getSolucion()));
        }
        evaluaciones += 50;
    }

    /**
     * Metodo principal del algoritmo genetico generacional con elitismo
     */
    public void ejecucion(int max) {
        time = System.nanoTime();
        int generacionesSinMejora = 0;

        while (evaluaciones < max) {

            //Torneo Binario
            torneoBinario();

            //Cruzamiento
            for (int i = 0; i < 18; i += 2) {
                Solucion padre = poblacionGanadores.get(i);
                Solucion madre = poblacionGanadores.get(i + 1);
                Solucion hijo1 = new Solucion();
                Solucion hijo2 = new Solucion();

                cruzamiento(padre, i, madre, i + 1, hijo1, hijo2);
                evaluaciones += 2;
            }
            //Mutacion
            int posMutacion = rd.nextInt(poblacionGanadores.values().size());
            mutacion(poblacionGanadores.get(posMutacion));
            poblacionGanadores.get(posMutacion).calculaRestriccion(data.getRestricciones());

            //Buscamos la mejor solucion, por si hay una nueva
            Solucion posibleMejor = new Solucion(calculaMejorsolucion(poblacionGanadores.values().toArray()));
            if (posibleMejor.getPuntuacion() < mejor.getPuntuacion()) {
                mejor = posibleMejor;
                generacionesSinMejora = 0;
            } else {
                ++generacionesSinMejora;
            }

            //Calculamos el numero de individuos diferentes dentro de la poblacion
            Vector<Integer> puntuaciones = new Vector<>();
            for (int i = 0; i < poblacionGanadores.size(); ++i) {
                if (!puntuaciones.contains(poblacionGanadores.get(i).getPuntuacion())) {
                    puntuaciones.add(poblacionGanadores.get(i).getPuntuacion());
                }
            }

            //Reinicializamos si no mejoramos en 20 generacion o el 80% de los individuos son el mismo
            if (generacionesSinMejora >= 20 || (puntuaciones.size() <= poblacionGanadores.size() * 0.2)) {
                generacionesSinMejora = 0;
                poblacion.clear();
                for (int i = 0; i < 50; ++i) {
                    miGreedy.generaSolucion();
                    poblacion.put(i, new Solucion(miGreedy.getSolucion()));
                }
                evaluaciones += 50;

                //Elitismo: si la mejor solución de la generación anterior no sobrevive, sustituye directamente la peor solución de la nueva población
                int posicionPeor = 0;
                for (int i = 0; i < poblacion.values().size(); ++i) {
                    if (poblacion.get(i).getPuntuacion() > poblacion.get(posicionPeor).getPuntuacion()) {
                        posicionPeor = i;
                    }
                }
                poblacion.remove(posicionPeor);
                poblacion.put(posicionPeor, new Solucion(mejor));

            } else {
                poblacion.clear();
                poblacion = new HashMap<>(poblacionGanadores);
            }
        }
        time = System.nanoTime() - time;
    }

    private void cruzamiento(Solucion padre, int posPadre, Solucion madre, int posMadre, Solucion hijo1, Solucion hijo2) {
        //Cruce normal
        if (!blx) {

            int desde = rd.nextInt(padre.getFrecuenciasAsignadas().size());
            int hasta = rd.nextInt(padre.getFrecuenciasAsignadas().size());
            if (desde > hasta) {
                int aux = hasta;
                hasta = desde;
                desde = aux;
            }

            for (FrecAsignada f : padre.getFrecuenciasAsignadas().values()) {
                if (f.getId() >= desde && f.getId() <= hasta) {
                    hijo1.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                    hijo2.getFrecuenciasAsignadas().put(f.getId(), f);
                } else {
                    hijo1.getFrecuenciasAsignadas().put(f.getId(), f);
                    hijo2.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                }
            }

            hijo1.calculaRestriccion(data.getRestricciones());
            hijo2.calculaRestriccion(data.getRestricciones());

            poblacionGanadores.remove(posPadre);
            poblacionGanadores.put(posPadre, new Solucion(hijo1));
            poblacionGanadores.get(posPadre).calculaRestriccion(data.getRestricciones());

            poblacionGanadores.remove(posMadre);
            poblacionGanadores.put(posMadre, new Solucion(hijo2));
            poblacionGanadores.get(posMadre).calculaRestriccion(data.getRestricciones());

            //Cruce con BLX
        } else {
            for (FrecAsignada frec : padre.getFrecuenciasAsignadas().values()) {

                int rangoNodo = data.getTransmisores().get(frec.getId()).getRango();
                int intervalo, nuevaFrecuencia;
                int frecPadre = frec.getFrecuencia();
                int frecMadre = madre.getFrecuenciasAsignadas().get(frec.getId()).getFrecuencia();
                //System.out.println("Padre: "+frecPadre+" Madre: "+frecMadre+" Nueva: "+frec.getId());
                if (frecPadre < frecMadre) {
                    intervalo = (int) ((frecMadre - frecPadre) * 0.5);
                    nuevaFrecuencia = rd.nextInt(frecMadre + intervalo);
                    nuevaFrecuencia += frecPadre;
                } else {
                    intervalo = (int) ((frecPadre - frecMadre) * 0.5);
                    nuevaFrecuencia = rd.nextInt(frecPadre + intervalo);
                    nuevaFrecuencia += frecMadre;
                }
                for (int i = 0; i < data.getFrecuencias().get(rangoNodo).tamanio(); ++i) {
                    if (data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i) > nuevaFrecuencia) {
                        nuevaFrecuencia = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                        break;
                    }
                    if (i == data.getFrecuencias().size() - 1) {
                        nuevaFrecuencia = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                    }
                }

                hijo1.anadeFrecuencia(new FrecAsignada(frec.getId(), nuevaFrecuencia));

                if (frecPadre < frecMadre) {
                    intervalo = (int) ((frecMadre - frecPadre) * 0.5);
                    nuevaFrecuencia = rd.nextInt(frecMadre + intervalo);
                    nuevaFrecuencia += frecPadre;
                } else {
                    intervalo = (int) ((frecPadre - frecMadre) * 0.5);
                    nuevaFrecuencia = rd.nextInt(frecPadre + intervalo);
                    nuevaFrecuencia += frecMadre;
                }
                for (int i = 0; i < data.getFrecuencias().get(rangoNodo).tamanio(); ++i) {
                    if (data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i) > nuevaFrecuencia) {
                        nuevaFrecuencia = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                        break;
                    }
                    if (i == data.getFrecuencias().size() - 1) {
                        nuevaFrecuencia = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                    }
                }

                hijo2.anadeFrecuencia(new FrecAsignada(frec.getId(), nuevaFrecuencia));
            }
        }
    }


    /**
     * Metodo para obtener la mejor de dos soluciones escogidas aleatoriamente, de la poblacion.
     */
    private void torneoBinario() {
        poblacionGanadores.clear();
        for (int i = 0; i < poblacion.size(); ++i) {
            int posContrincante1 = rd.nextInt(poblacion.size());
            int posContrincante2 = rd.nextInt(poblacion.size());
            if (poblacion.get(posContrincante1).getPuntuacion() < poblacion.get(posContrincante2).getPuntuacion()) {
                poblacionGanadores.put(i, new Solucion(poblacion.get(posContrincante1)));
            } else {
                poblacionGanadores.put(i, new Solucion(poblacion.get(posContrincante2)));
            }
        }
    }

    /**
     * Metodo para obtener la mejor solucion dado un array.
     *
     * @param poblaciones Array de objetos que tendra la poblacion de soluciones.
     */
    private Solucion calculaMejorsolucion(Object[] poblaciones) {
        Solucion masBaja = new Solucion();
        masBaja.setPuntuacion(999999);

        for (Object sol : poblaciones) {
            Solucion s = (Solucion) sol;
            if (s.getPuntuacion() < masBaja.getPuntuacion()) masBaja = s;
        }
        return masBaja;
    }

    /**
     * Metodo simple para mostrar por pantalla la puntuacion y el tiempo de ejecucion del algoritmo
     */
    public void mostrarResultados() {
        if (!blx) {
            System.out.println("AGG sin BLX, Puntuacion Mejor: " + mejor.getPuntuacion() + " Tiempo de ejecucion: " + time / 1000000 + " ms");
        } else {
            System.out.println("AGG con BLX, Puntuacion Mejor: " + mejor.getPuntuacion() + " Tiempo de ejecucion: " + time / 1000000 + " ms");
        }
    }

    /**
     * Metodo para mutar una solucion dada
     *
     * @param hijo Solucion para mutar
     */
    private void mutacion(Solucion hijo) {
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