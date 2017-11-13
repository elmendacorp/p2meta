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
            for (int i = 0; i < 18; ++i) {
                //Seleccionamos a los padres
                padre = (Solucion) poblacion.toArray()[cruces.get(i).getKey()];
                madre = (Solucion) poblacion.toArray()[cruces.get(i).getValue()];
                hijo = new Solucion();

            }
        }
    }
}
