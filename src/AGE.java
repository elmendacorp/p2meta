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

            int mutados = 0;
            //mutacion de los hijos

            //probabilidad de mutacion del cromosoma
            if (rd.nextDouble() < 0.02) {
                for (FrecAsignada f : hijo1.getFrecuenciasAsignadas().values()) {
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

            if (rd.nextDouble() < 0.02) {
                for (FrecAsignada f : hijo2.getFrecuenciasAsignadas().values()) {
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
            hijo1.calculaRestriccion(data.getRestricciones());
            hijo2.calculaRestriccion(data.getRestricciones());

            int reemplazo = 0;
            int idReemplazo=0;
            for (int i = 0; i < poblacion.size(); ++i) {
                if (poblacion.get(i).getKey() > reemplazo) {
                    reemplazo = poblacion.get(i).getKey();
                    idReemplazo=i;
                }
            }
            if(poblacion.get(idReemplazo).getKey()>hijo1.getPuntuacion()){
                poblacion.remove(idReemplazo);
                poblacion.add(new Pair<>(hijo1.getPuntuacion(),hijo1));
            }

            reemplazo = 0;
            idReemplazo=0;
            for (int i = 0; i < poblacion.size(); ++i) {
                if (poblacion.get(i).getKey() > reemplazo) {
                    reemplazo = poblacion.get(i).getKey();
                    idReemplazo=i;
                }
            }
            if(poblacion.get(idReemplazo).getKey()>hijo2.getPuntuacion()){
                poblacion.remove(idReemplazo);
                poblacion.add(new Pair<>(hijo2.getPuntuacion(),hijo2));
            }

            int mayor = 999999999;
            for (int i = 0; i < poblacion.size(); ++i) {
                if (poblacion.get(i).getKey() < mayor) {
                    mayor = poblacion.get(i).getKey();
                }
            }


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

            //System.out.println("Puntuacion Mejor: " + mayor + " Generacion: " + generacion+" Media: "+puntuacionNuevaGeneracion);

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
                //System.out.println("Reinicializa");
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

            for(FrecAsignada frec:padre.getFrecuenciasAsignadas().values()){

                int rangoNodo = data.getTransmisores().get(frec.getId()).getRango();
                int intervalo;
                int nuevaFrecuencia;
                int frecPadre=frec.getFrecuencia();
                int frecMadre=madre.getFrecuenciasAsignadas().get(frec.getId()).getFrecuencia();
                //System.out.println("Padre: "+frecPadre+" Madre: "+frecMadre+" Nueva: "+frec.getId());
                if(frecPadre<frecMadre){
                    intervalo= (int)((frecMadre-frecPadre)*0.5);
                    nuevaFrecuencia=rd.nextInt(frecMadre+intervalo);
                    nuevaFrecuencia+=frecPadre;
                }else{
                    intervalo=(int)((frecPadre-frecMadre)*0.5);
                    nuevaFrecuencia=rd.nextInt(frecPadre+intervalo);
                    nuevaFrecuencia+=frecMadre;
                }
                for(int i=0;i<data.getFrecuencias().get(rangoNodo).tamanio();++i){
                    if(data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i)>nuevaFrecuencia){
                        nuevaFrecuencia=data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                        break;
                    }
                    if(i==data.getFrecuencias().size()-1){
                        nuevaFrecuencia=data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                    }
                }


                hijo1.anadeFrecuencia(new FrecAsignada(frec.getId(),nuevaFrecuencia));

                if(frecPadre<frecMadre){
                    intervalo= (int)((frecMadre-frecPadre)*0.5);
                    nuevaFrecuencia=rd.nextInt(frecMadre+intervalo);
                    nuevaFrecuencia+=frecPadre;
                }else{
                    intervalo=(int)((frecPadre-frecMadre)*0.5);
                    nuevaFrecuencia=rd.nextInt(frecPadre+intervalo);
                    nuevaFrecuencia+=frecMadre;
                }
                for(int i=0;i<data.getFrecuencias().get(rangoNodo).tamanio();++i){
                    if(data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i)>nuevaFrecuencia){
                        nuevaFrecuencia=data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                        break;
                    }
                    if(i==data.getFrecuencias().size()-1){
                        nuevaFrecuencia=data.getFrecuencias().get(rangoNodo).getFrecuencias().get(i);
                    }
                }
                hijo2.anadeFrecuencia(new FrecAsignada(frec.getId(),nuevaFrecuencia));


            }
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
