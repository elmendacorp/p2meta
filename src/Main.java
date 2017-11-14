/**
 * @Author Rafael Martinez Rubio
 * @Mail rmr00034@red.ujaen.es
 * @Author Francisco Jesus Ruiz Lopez
 * @Mail fjrl0016@red.ujaen.es
 */

import java.util.Vector;

/**
 * Clase para la prueba de ejecucion sobre un unico conjunto de datos
 */
public class Main {

    public static final int SEMILLA1 = 77361422;
    public static final int SEMILLA2 = 23456781;
    public static final int SEMILLA3 = 36142277;
    public static final int SEMILLA4 = 45678123;
    public static final int SEMILLA5 = 14227736;
    public static final int SEMILLARETO = 21025923;

    public static Vector<Solucion> soluciones= new Vector<>();

    public static void main(String[] args) {

        Filemanager filemanager1 = new Filemanager("./archivos_guion/instancias/graph05/");
        Filemanager filemanager2 = new Filemanager("./archivos_guion/instancias/graph06/");
        Filemanager filemanager3 = new Filemanager("./archivos_guion/instancias/graph07/");
        Filemanager filemanager4 = new Filemanager("./archivos_guion/instancias/graph11/");
        Filemanager filemanager5 = new Filemanager("./archivos_guion/instancias/graph12/");
        Filemanager filemanager6 = new Filemanager("./archivos_guion/instancias/graph13/");
        Filemanager filemanager7 = new Filemanager("./archivos_guion/instancias/scen06/");
        Filemanager filemanager8 = new Filemanager("./archivos_guion/instancias/scen07/");
        Filemanager filemanager9 = new Filemanager("./archivos_guion/instancias/scen08/");
        Filemanager filemanager10 = new Filemanager("./archivos_guion/instancias/scen09/");
        Filemanager filemanager11 = new Filemanager("./archivos_guion/instancias/scen10/");



        Filemanager fileFinal = filemanager2;
        //fileFinal.imprimeDatos();
        int semillaFinal = SEMILLA1;


        AGE miAGE = new AGE(fileFinal,semillaFinal);
        miAGE.inicializacion();
        miAGE.puntuacionesPoblacion();
        miAGE.ejecucion(20000,2);
        miAGE.puntuacionesPoblacion();

    }
}
