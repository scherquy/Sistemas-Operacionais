package simulador.main;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class TesteLiberacaoRandom{
    public static void main(String[] args) {
        Heap heap = new Heap(1); // 1 KB = 256 slots
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        // cada bloco terá 64 bytes = 16 slots
        for (int i = 0; i < 8; i++) {
            gerenciador.alocarFirstFit(64);
        }

        int totalSlots = heap.getTotalSlots();
        int metaLiberacao = (int) Math.ceil(totalSlots * 0.30);

        int livresAntes = gerenciador.contarSlotsLivresPelaHeap();

        System.out.printf("\n\n===== ANTES DA LIBERAÇÃO =====");
        System.out.printf("\nTotal de slots da heap: %d", totalSlots);
        System.out.printf("\nMeta mínima de liberação: %d slots", metaLiberacao);
        System.out.printf("\nSlots livres antes: %d\n", livresAntes);
        gerenciador.imprimirTabelaAlocacoes();

        gerenciador.liberarAleatorio();

        int livresDepois = gerenciador.contarSlotsLivresPelaHeap();
        int slotsLiberados = livresDepois - livresAntes;

        System.out.printf("\n\n===== DEPOIS DA LIBERAÇÃO =====");
        System.out.printf("\nSlots livres depois: %d", livresDepois);
        System.out.printf("\nSlots liberados: %d", slotsLiberados);
        System.out.printf("\nMeta mínima exigida: %d", metaLiberacao);

        if (slotsLiberados >= metaLiberacao) {
            System.out.printf("\nRESULTADO OK - liberou pelo menos 30%% da heap\n");
        }   else{
                System.out.printf("\nERRO - liberou menos de 30%% da heap\n");
            }

        gerenciador.imprimirTabelaAlocacoes();
    }
}