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
        evaluaciones = 50;
    }

    public void ejecucion(int max) {
        time = System.nanoTime();
        double puntuacionGeneracionAnterior = 0;
        int generacionesSinMejora = 0;
        int generacion = 0;
        while (evaluaciones < max) {


            for (int i = 0; i < poblacion.size(); ++i) {
                puntuacionGeneracionAnterior += poblacion.get(i).getValue().getPuntuacion();
            }
            puntuacionGeneracionAnterior = puntuacionGeneracionAnterior / poblacion.size();

            //torneo binario
            Vector<Pair<Integer,Solucion>> torneo=new Vector<>();

            //seleccion de los peores padres

            Vector<Pair<Integer, Integer>> cruces = new Vector<>();
            //seleccionamos 25 parejas que van a cruzarse

            for (int i = 0; i < 25; ++i) {
                int padre = rd.nextInt(50);
                int madre = rd.nextInt(50);
                //con esto evitamos que se crucen consigo mismo
                while (padre == madre) {
                    madre = rd.nextInt(50);
                }
                cruces.add(new Pair<>(padre, madre));
            }


            //cruzamos los padres para generar los nuevos hijos
            Vector<Hijo> descendientes = new Vector<>();
            Solucion padre, madre, hijo1, hijo2;

            //cruce de los padres
            for (int i = 0; i < 25; ++i) {
                padre = poblacion.get(cruces.get(i).getKey()).getValue();
                madre = poblacion.get(cruces.get(i).getKey()).getValue();
                hijo1 = new Solucion();
                hijo2 = new Solucion();
                cruzamiento(padre, madre, hijo1, hijo2, 1);
                descendientes.add(new Hijo(cruces.get(i).getKey(), cruces.get(i).getValue(), hijo1));
                descendientes.add(new Hijo(cruces.get(i).getKey(), cruces.get(i).getValue(), hijo2));
            }


            evaluaciones += 25;
            int mutados = 0;
            //mutacion de los hijos
            for (Hijo h : descendientes) {
                //probabilidad de mutacion del cromosoma
                if (rd.nextDouble() < 0.02) {
                    for (FrecAsignada f : h.getSolucion().getFrecuenciasAsignadas().values()) {
                        //probabilidad de mutacion del gen
                        if (rd.nextDouble() < 0.1) {
                            int rangoNodo = data.getTransmisores().get(f.getId()).getRango();
                            int rangoTam = data.getFrecuencias().get(rangoNodo).getFrecuencias().size();
                            int frecNodo = data.getFrecuencias().get(rangoNodo).getFrecuencias().get(rd.nextInt(rangoTam));
                            f.setFrecuencia(frecNodo);
                            ++mutados;
                        }
                    }
                }
                h.getSolucion().calculaRestriccion(data.getRestricciones());
            }

            for (Hijo h : descendientes) {
                for (int i = 0; i < poblacion.size(); ++i) {
                    if (poblacion.get(i).getValue().getPuntuacion() > h.getSolucion().getPuntuacion()) {
                        poblacion.remove(i);
                        poblacion.add(new Pair<>(h.getSolucion().getPuntuacion(), new Solucion(h.getSolucion())));
                        break;
                    }
                }
            }


            int mayor = 999999999;
            for (int i = 0; i < poblacion.size(); ++i) {
                if (poblacion.get(i).getKey() < mayor) {
                    mayor = poblacion.get(i).getKey();
                }
            }
            System.out.println("Puntuacion Mejor: " + mayor+ " Generacion: "+generacion);

            //calculos para la reinicializacion
            Vector<Integer> puntuaciones = new Vector<>();

            double puntuacionNuevaGeneracion = 0;
            for (int i = 0; i < poblacion.size(); ++i) {
                puntuacionNuevaGeneracion += poblacion.get(i).getValue().getPuntuacion();
                if (!puntuaciones.contains(poblacion.get(i).getValue().getPuntuacion())) {
                    puntuaciones.add(poblacion.get(i).getValue().getPuntuacion());
                }
            }
            puntuacionNuevaGeneracion = puntuacionNuevaGeneracion / poblacion.size();

            if (puntuacionGeneracionAnterior >= puntuacionNuevaGeneracion) {
                ++generacionesSinMejora;
            } else {
                generacionesSinMejora = 0;
            }

            Solucion mejor;
            int indiceMejor = 0;
            int puntuacionMejor = 999999999;

            if (generacionesSinMejora >= 20 || (puntuaciones.size() <= poblacion.size() * 0.2)) {
                generacionesSinMejora = 0;
                System.out.println("Reinicializa");
                for (int i = 0; i < poblacion.size(); ++i) {
                    if (poblacion.get(i).getKey() < puntuacionMejor) {
                        indiceMejor = i;
                        puntuacionMejor = poblacion.get(i).getKey();
                    }
                }
                mejor = new Solucion(poblacion.get(indiceMejor).getValue());
                poblacion = new Vector<>();
                poblacion.add(new Pair<>(mejor.getPuntuacion(), mejor));
                for (int i = 0; i < 49; ++i) {
                    miGreedy.generaSolucion();
                    poblacion.add(new Pair<>(miGreedy.getSolucion().getPuntuacion(), miGreedy.getSolucion()));
                }
                evaluaciones += 50;
            }

            ++generacion;
            //System.out.println("Generacion "+generacion+" Mutaciones "+mutados);


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
        }
    }

    public void puntuacionesPoblacion() {
        int mejor = 999999999;
        for (int i = 0; i < poblacion.size(); ++i) {
            if(poblacion.get(i).getKey()<mejor){
                mejor=poblacion.get(i).getKey();
            }
        }
        System.out.println("Puntuacion Mejor: " + mejor + " Tiempo de ejecucion: " + time / 1000000 + " ms");
    }

}
