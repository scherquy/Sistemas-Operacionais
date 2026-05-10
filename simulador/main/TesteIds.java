package simulador.main;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class TesteIds {
    public static void main(String[] args) {
        Heap heap = new Heap(1); // 1 KB = 1024 bytes = 256 slots
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        int id1 = gerenciador.alocarFirstFit(16); // 16 bytes = 4 slots
        int id2 = gerenciador.alocarFirstFit(12); // 12 bytes = 3 slots
        int id3 = gerenciador.alocarFirstFit(20); // 20 bytes = 5 slots

        System.out.printf("\nIDs gerados:");
        System.out.printf("\nID 1: %d", id1);
        System.out.printf("\nID 2: %d", id2);
        System.out.printf("\nID 3: %d\n", id3);

        heap.imprimirEstado(20);
        gerenciador.imprimirTabelaAlocacoes();
    }
}