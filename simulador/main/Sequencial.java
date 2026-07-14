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

    //recebe a lista de requisições
    public Sequencial(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int[] tamanhosRequisicoes) {
        this.tamanhoHeapKB = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
    }

    // executa a versão sequencial e retorna as estatísticas
    public Estatisticas executar() {
        System.out.printf("\nSIMULADOR SEQUENCIAL\n");
        System.out.printf("Heap: %d KB | Requisições: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);

        Heap heap = new Heap(tamanhoHeapKB); //cria a heap
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap); //cria o gerenciador da heap

        GerenciadorRequisicoes gerador; // objeto que processa as requisições

        gerador = new GerenciadorRequisicoes(gerenciador, tamanhosRequisicoes); //usa essa existente

        return gerador.executar(); //executa as requisições e retorna as estatísticas
    }
}