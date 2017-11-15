import javafx.util.Pair;

import java.util.*;


public class AGE {
    private Vector<Pair<Integer, Solucion>> poblacion;
    private float time;
    private Random rd;
    private Filemanager data;
    private int semilla;
    private int evaluaciones;
    private Greedy miGreedy;


    /**
     * Constructor parametrizado de la clase
     *
     * @param datos   conjunto de datos a explotar
     * @param semilla semilla para el aleatorio
     */
    public AGE(Filemanager datos, int semilla) {
        poblacion = new Vector<>();
        this.semilla = semilla;
        data = datos;
        rd = new Random();
        rd.setSeed(semilla);
        evaluaciones = 0;
    }

    /**
     * Metodo para la inicializacion de la poblacion
     */
    public void inicializacion() {
        miGreedy = new Greedy(data, semilla);
        for (int i = 0; i < 50; ++i) {
            miGreedy.generaSolucion();
            poblacion.add(new Pair<>(miGreedy.getSolucion().getPuntuacion(), miGreedy.getSolucion()));
        }
        //informacion de depuracion para ver que las soluciones se crean adecuadamente
        /*
        for(Pair<Integer,Solucion> p:poblacion){
            for(FrecAsignada f: p.getValue().getFrecuenciasAsignadas().values()){
                System.out.println("["+f.getId()+" "+f.getFrecuencia()+"]");
            }
        }
        */
        evaluaciones = 50;
    }

    public void ejecucion(int max, int tipo) {
        time = System.nanoTime();
        double puntuacionGeneracionAnterior = 0;
        int generacionesSinMejora = 0;
        int generacion = 0;
        while (evaluaciones < max) {


            for (int i = 0; i < poblacion.size(); ++i) {
                puntuacionGeneracionAnterior += poblacion.get(i).getValue().getPuntuacion();
            }
            puntuacionGeneracionAnterior = puntuacionGeneracionAnterior / poblacion.size();

            //seleccionamos la pareja que va a cruzarse

            Solucion padre, madre;
            int contrincante1 = rd.nextInt(50);
            int contrincante2 = rd.nextInt(50);
            if (poblacion.get(contrincante1).getValue().getPuntuacion() < poblacion.get(contrincante2).getValue().getPuntuacion()) {
                padre = poblacion.get(contrincante1).getValue();
            } else {
                padre = poblacion.get(contrincante2).getValue();
            }
            contrincante1 = rd.nextInt(50);
            contrincante2 = rd.nextInt(50);
            if (poblacion.get(contrincante1).getValue().getPuntuacion() < poblacion.get(contrincante2).getValue().getPuntuacion()) {
                madre = poblacion.get(contrincante1).getValue();
            } else {
                madre = poblacion.get(contrincante2).getValue();
            }

            Solucion hijo1, hijo2;

            //cruce de los padres
            hijo1 = new Solucion();
            hijo2 = new Solucion();
            cruzamiento(padre, madre, hijo1, hijo2, tipo);

            evaluaciones += 2;

            //mutacion de los hijos

            mutacionDescendiente(hijo1);
            mutacionDescendiente(hijo2);

            hijo1.calculaRestriccion(data.getRestricciones());
            hijo2.calculaRestriccion(data.getRestricciones());

            //reemplazo dentro de la poblacion
            reemplazoPoblacion(hijo1);
            reemplazoPoblacion(hijo2);

            //mejor solucion de la poblacion
            int mayor = 999999999;
            for (int i = 0; i < poblacion.size(); ++i) {
                if (poblacion.get(i).getKey() < mayor) {
                    mayor = poblacion.get(i).getKey();
                }
            }


            //calculos para la reinicializacion
            Vector<Integer> puntuaciones = new Vector<>();


            //calculo del numero de individuos diferentes dentro de la poblacion
            double puntuacionNuevaGeneracion = 0;
            for (int i = 0; i < poblacion.size(); ++i) {
                puntuacionNuevaGeneracion += poblacion.get(i).getValue().getPuntuacion();
                if (!puntuaciones.contains(poblacion.get(i).getValue().getPuntuacion())) {
                    puntuaciones.add(poblacion.get(i).getValue().getPuntuacion());
                }
            }
            puntuacionNuevaGeneracion = puntuacionNuevaGeneracion / poblacion.size();

            //System.out.println("Puntuacion Mejor: " + mayor + " Generacion: " + generacion+" Media: "+puntuacionNuevaGeneracion);

            if (puntuacionGeneracionAnterior >= puntuacionNuevaGeneracion) {
                ++generacionesSinMejora;
            } else {
                generacionesSinMejora = 0;
            }


            Solucion mejor;
            int indiceMejor = 0;
            int puntuacionMejor = 999999999;

            //reinicializacion de la poblacion
            if (generacionesSinMejora >= 20 || (puntuaciones.size() <= poblacion.size() * 0.2)) {
                generacionesSinMejora = 0;
                //System.out.println("Reinicializa");
                //seleccion del mejor candidato
                for (int i = 0; i < poblacion.size(); ++i) {
                    if (poblacion.get(i).getKey() < puntuacionMejor) {
                        indiceMejor = i;
                        puntuacionMejor = poblacion.get(i).getKey();
                    }
                }
                mejor = new Solucion(poblacion.get(indiceMejor).getValue());
                poblacion = new Vector<>();
                poblacion.add(new Pair<>(mejor.getPuntuacion(), mejor));
                //se completa la poblacion con soluciones greedy
                for (int i = 0; i < 49; ++i) {
                    miGreedy.generaSolucion();
                    poblacion.add(new Pair<>(miGreedy.getSolucion().getPuntuacion(), miGreedy.getSolucion()));
                }
                evaluaciones += 50;
            }

            ++generacion;


        }
        time = System.nanoTime() - time;
    }

    /**
     * Cruce entre los padre(noche de pasion)
     *
     * @param padre papa
     * @param madre mama
     * @param hijo1 hijo
     * @param hijo2 hijo
     * @param tipo  1 para dos puntos, otra cosa para blx
     */
    private void cruzamiento(Solucion padre, Solucion madre, Solucion hijo1, Solucion hijo2, int tipo) {
        if (tipo == 1) {
            int inicio, fin;
            inicio = rd.nextInt(padre.getFrecuenciasAsignadas().size());
            fin = rd.nextInt(padre.getFrecuenciasAsignadas().size());
            if (inicio > fin) {
                int tmp = inicio;
                inicio = fin;
                fin = tmp;
            }
            //sustituimos las posiciones por sus respectivos identificadores de transmisor
            inicio = (Integer) padre.getFrecuenciasAsignadas().keySet().toArray()[inicio];
            fin = (Integer) padre.getFrecuenciasAsignadas().keySet().toArray()[fin];
            for (FrecAsignada f : padre.getFrecuenciasAsignadas().values()) {
                if (f.getId() >= inicio && f.getId() <= fin) {
                    hijo1.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                    hijo2.getFrecuenciasAsignadas().put(f.getId(), f);
                } else {
                    hijo1.getFrecuenciasAsignadas().put(f.getId(), f);
                    hijo2.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(), madre.getFrecuenciasAsignadas().get(f.getId()));
                }
            }
        } else {

            for (FrecAsignada frec : padre.getFrecuenciasAsignadas().values()) {

                int rangoNodo = data.getTransmisores().get(frec.getId()).getRango();
                int intervalo;
                int nuevaFrecuencia;
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
     * Funcion para aplicar la mutacion a los hijos
     *
     * @param hijo
     */

    public void mutacionDescendiente(Solucion hijo) {
        if (rd.nextDouble() < 0.02) {
            for (FrecAsignada f : hijo.getFrecuenciasAsignadas().values()) {
                //probabilidad de mutacion del gen
                if (rd.nextDouble() < 0.1) {
                    int rangoNodo = data.getTransmisores().get(f.getId()).getRango();
                    int rangoTam = data.getFrecuencias().get(rangoNodo).getFrecuencias().size();
                    int frecNodo = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(rd.nextInt(rangoTam));
                    f.setFrecuencia(frecNodo);

                }
            }
        }
    }

    /**
     * Funcion para reemplazar la peor solucion dentro de la poblacion actual
     *
     * @param candidato
     */

    public void reemplazoPoblacion(Solucion candidato) {
        int reemplazo = 0;
        int idReemplazo = 0;
        for (int i = 0; i < poblacion.size(); ++i) {
            if (poblacion.get(i).getKey() > reemplazo) {
                reemplazo = poblacion.get(i).getKey();
                idReemplazo = i;
            }
        }
        if (poblacion.get(idReemplazo).getKey() > candidato.getPuntuacion()) {
            poblacion.remove(idReemplazo);
            poblacion.add(new Pair<>(candidato.getPuntuacion(), candidato));
        }
    }

    public void puntuacionesPoblacion() {
        int mejor = 999999999;
        for (int i = 0; i < poblacion.size(); ++i) {
            if (poblacion.get(i).getKey() < mejor) {
                mejor = poblacion.get(i).getKey();
            }
        }
        System.out.println("AGE Puntuacion Mejor: " + mejor + " Tiempo de ejecucion: " + time / 1000000 + " ms");
    }

}
