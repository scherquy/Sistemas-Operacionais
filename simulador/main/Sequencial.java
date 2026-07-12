package simulador.main;

import simulador.estatisticas.Estatisticas;
import simulador.heap.GerenciadorHeap;
import simulador.heap.GerenciadorRequisicoes;
import simulador.heap.Heap;

public class Sequencial {
    private final int tamanhoHeapKB;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;
    private final int[] tamanhosRequisicoes;

    public Sequencial(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes) {
        this(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, null);
    }

    public Sequencial(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int[] tamanhosRequisicoes) {
        this.tamanhoHeapKB = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
    }

    public Estatisticas executar() {
        System.out.printf("\nSIMULADOR SEQUENCIAL\n");
        System.out.printf("Heap: %d KB | Requisições: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);

        Heap heap = new Heap(tamanhoHeapKB);
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        GerenciadorRequisicoes gerador;

        if (tamanhosRequisicoes == null) {
            gerador = new GerenciadorRequisicoes(gerenciador, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes);
        } else {
            gerador = new GerenciadorRequisicoes(gerenciador, tamanhosRequisicoes);
        }

        return gerador.executar();
    }
}