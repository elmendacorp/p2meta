public class Hijo{
    private int padre;
    private int madre;
    private Solucion solucion;

    public Hijo(int padre,int madre, Solucion solucion ){
        this.padre=padre;
        this.madre=madre;
        this.solucion= solucion;
    }
    public int getMadre(){return madre;}

    public int getPadre(){return padre;}

    public Solucion getSolucion() {
        return solucion;
    }
}
