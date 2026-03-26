package sdjini.AirDMM.custom;

public class CoupleQueue {
    public final Object lock = new Object();

    static {
        System.loadLibrary("Couple");
    }

    public CoupleQueue(int capacity) {
        INIT(capacity);
    }

    public native void INIT(int capacity);

    public native void add(String a, String b);
    public native String[] get();

    public native boolean isEmpty();

    public native void destroy();

    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
}
