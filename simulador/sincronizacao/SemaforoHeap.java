package simulador.sincronizacao;

import java.util.concurrent.Semaphore;

public class SemaforoHeap {
    private final Semaphore mutex;

    public SemaforoHeap() {
        this.mutex = new Semaphore(1, true);
    }

    public void adquirir() {
        mutex.acquireUninterruptibly();
    }

    public boolean tentarAdquirir() {
        return mutex.tryAcquire();
    }

    public void liberar() {
        mutex.release();
    }
}