package sdjini.AirDMM.custom;

public class CoupleQueue {
    public final Object lock = new Object();

    static {
        System.loadLibrary("Couple");
    }

    public CoupleQueue(int capacity) {
        INIT(capacity);
    }

    // 初始化：预设队列容量
    public native void INIT(int capacity);

    // 添加一对字符串
    public native void add(String a, String b);

    // 获取并移除头部的一对字符串，返回 String[2]
    public native String[] get();

    // 判空
    public native boolean isEmpty();

    // 销毁资源
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
