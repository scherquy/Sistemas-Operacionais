package simulador.sincronizacao;

import java.util.concurrent.Semaphore;



public class SemaforoHeap {
    
    private final Semaphore mutex;

    public SemaforoHeap(){
        // O TRUE garante que as threads vão adquirir o semáforo por ordem de chegada (FIFO),
        this.mutex = new Semaphore(1,true);
    }
  
    //Bloque a thread atual até qeu ela consigo acesso exclusio a heap.

    public void adquirir(){
        try{
            mutex.acquire();

        } catch(InterruptedException e){
            System.err.println("[SEMAFORO] A thread foi interrompiada enquanto aguardava a Heap.");
            Thread.currentThread().interrupt();
        }
    }
    //Libera a o MUTEX para próxima Thread entrar na Heap
    public void liberar(){
        mutex.release();
    }




}
