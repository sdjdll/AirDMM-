package sdjini.AirDMM.custom;
/// Doing: add
/// TODO: get
/// TODO: isEmpty
public class Queue<Element> {
    private int length,size;
    private Element[] elements;
    static {
        System.loadLibrary("libQueue");
    }
    public Queue(int size){
        this.size = size;
        this.length = 0;
        INIT();
    }
    private native void INIT();
    native boolean add(Element element);
}
