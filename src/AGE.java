import javafx.util.Pair;

import java.util.*;


public class AGE {
    private Set<Solucion> poblacion;
    private float time;
    private Random rd;
    private Filemanager data;
    private int semilla;
    private int evaluaciones;


    /**
     * Constructor parametrizado de la clase
     *
     * @param datos   conjunto de datos a explotar
     * @param semilla semilla para el aleatorio
     */
    public AGE(Filemanager datos, int semilla) {
        poblacion = new HashSet<>();
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
        Greedy miGreedy = new Greedy(data, semilla);
        for (int i = 0; i < 50; ++i) {
            miGreedy.generaSolucion();
            poblacion.add(miGreedy.getSolucion());
        }
        evaluaciones = 50;
    }

    public void ejecucion(int max) {
        while (evaluaciones < max) {
            Vector<Pair<Integer, Integer>> cruces = new Vector<>();
            //seleccionamos 18 parejas que van a cruzarse
            for (int i = 0; i < 18; ++i) {
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
            Solucion padre,madre,hijo;
            for (int i = 0; i < 1; ++i) {
                //Seleccionamos a los padres
                padre = (Solucion) poblacion.toArray()[cruces.get(i).getKey()];
                madre = (Solucion) poblacion.toArray()[cruces.get(i).getValue()];
                hijo = new Solucion();
                cruzamiento(padre,madre,hijo,1);
                descendientes.add(new Hijo(cruces.get(i).getKey(),cruces.get(i).getValue(),hijo));

            }
            evaluaciones+=10000;
        }

    }

    /**
     * Cruce entre los padre(noche de pasion)
     * @param padre papa
     * @param madre mama
     * @param hijo hijo
     * @param tipo 1 para dos puntos, otra cosa para blx
     */
    private void cruzamiento(Solucion padre,Solucion madre, Solucion hijo, int tipo){
        if(tipo==1){
            int inicio,fin;
            inicio=rd.nextInt(padre.getFrecuenciasAsignadas().size());
            fin=rd.nextInt(padre.getFrecuenciasAsignadas().size());
            if(inicio>fin){
                int tmp=inicio;
                inicio=fin;
                fin=tmp;
            }
            //sustituimos las posiciones por sus respectivos identificadores de transmisor
            inicio=(Integer)padre.getFrecuenciasAsignadas().keySet().toArray()[inicio];
            fin=(Integer)padre.getFrecuenciasAsignadas().keySet().toArray()[fin];
            for(FrecAsignada f:padre.getFrecuenciasAsignadas().values()){
                if(f.getId()>=inicio &&f.getId()<=fin){
                    hijo.getFrecuenciasAsignadas().put(madre.getFrecuenciasAsignadas().get(f.getId()).getId(),madre.getFrecuenciasAsignadas().get(f.getId()));
                }else{
                    hijo.getFrecuenciasAsignadas().put(f.getId(),f);
                }
            }
        }
    }
}
