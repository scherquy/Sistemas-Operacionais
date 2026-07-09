package simulador.sincronizacao;

import java.util.concurrent.Semaphore;

public class SemaforoHeap {

     // Este semáforo é usado para proteger o acesso a heap.
     // A heap é compartilhada por várias threads, então só uma thread deve alterar a heap por vez.
     // Por isso usamos um semáforo binário.
    private final Semaphore mutex;

    public SemaforoHeap() {

         // O 1 diz só uma thread entra na seção crítica.
         // O true garante que as threads sejam atendidas por ordem de chegada.
         // Isso reduz o risco de starvation.
        this.mutex = new Semaphore(1, true);
    }

    
     // Deve ser chamado antes de acessar a heap.
     // Ele bloqueia a thread até que ela consiga entrar na seção crítica.
    public boolean adquirir() {
        try {
            mutex.acquire();
            return true;
        } catch (InterruptedException e) {
            System.err.printf("\n[SEMAFORO] A thread foi interrompida enquanto aguardava acesso a heap.\n");
            Thread.currentThread().interrupt();
            return false;
        }
    }

     // Deve ser chamado depois que a thread terminar de usar a heap.
     // Ele libera a permissão para que outra thread possa entrar.
    public void liberar() {
        mutex.release();
    }
}