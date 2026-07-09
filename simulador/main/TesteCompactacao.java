package simulador.main;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class TesteCompactacao {
    public static void main(String[] args) {
        Heap heap = new Heap(1); // 1 KB = 256 slots
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        int id1 = gerenciador.alocarFirstFit(16); // 16 bytes = 4 slots
        int id2 = gerenciador.alocarFirstFit(16); // 16 bytes = 4 slots
        int id3 = gerenciador.alocarFirstFit(16); // 16 bytes = 4 slots

        System.out.println("\nTESTE DE COMPACTAÇÃO");

        System.out.println("\nIDs alocados:");
        System.out.println("ID 1: " + id1);
        System.out.println("ID 2: " + id2);
        System.out.println("ID 3: " + id3);

        System.out.println("\nEstado inicial da heap:");
        heap.imprimirEstado(20);
        gerenciador.imprimirTabelaAlocacoes();

        System.out.println("\nLiberando o bloco do meio: ID " + id2);
        gerenciador.liberarPorId(id2);

        System.out.println("\nEstado da heap após liberar o ID " + id2 + ":");
        heap.imprimirEstado(20);
        gerenciador.imprimirTabelaAlocacoes();

        System.out.println("\nChamando compactação");
        gerenciador.compactar();

        System.out.println("\nEstado da heap após compactação:");
        heap.imprimirEstado(20);
        gerenciador.imprimirTabelaAlocacoes();

        int[] memoria = heap.getMemoria();

        boolean compactacaoCorreta = memoria[0] == id1 && memoria[1] == id1 && memoria[2] == id1 && memoria[3] == id1 && memoria[4] == id3 && memoria[5] == id3 && memoria[6] == id3 && memoria[7] == id3 && memoria[8] == 0 && memoria[9] == 0 && memoria[10] == 0 && memoria[11] == 0;

        if (compactacaoCorreta) {
            System.out.println("\nRESULTADO OK - A compactação removeu o buraco corretamente");
        }   else {
                System.out.println("\nERRO - A compactação não produziu o resultado esperado");
            }
    }
}