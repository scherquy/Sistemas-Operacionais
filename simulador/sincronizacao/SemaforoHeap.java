package simulador.sincronizacao;

import java.util.concurrent.Semaphore;

public class SemaforoHeap {
    private final Semaphore mutex; //declara o semaforo

    //cria o semaforo com uma permissão
    public SemaforoHeap() {
        this.mutex = new Semaphore(1, true); // 1 = uma thread passa por vez pelo semaforo. true respeita a ordem de chegada das threads
    }

    // a thread espera até conseguir pegar o semáforo
    public void adquirir() {
        mutex.acquireUninterruptibly();
    }

    // tenta pegar um semáforo sem bloquear. Nos segmentos da heap se um está ocupado a thread pode tentar pegar outro
    public boolean tentarAdquirir() {
        return mutex.tryAcquire();
    }

    //libera o semáforo para outra thread usar
    public void liberar() {
        mutex.release();
    }
}