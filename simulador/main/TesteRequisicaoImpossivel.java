package simulador.main;

import simulador.heap.GerenciadorHeap;
import simulador.heap.GerenciadorRequisicoes;
import simulador.heap.Heap;

public class TesteRequisicaoImpossivel {
    public static void main(String[] args) {
        System.out.println("\n===== TESTE DE REQUISIÇÃO IMPOSSÍVEL =====");

        int tamanhoHeapKB = 64;
        int tamanhoMinimoBytes = 70000;
        int tamanhoMaximoBytes = 70000;
        int totalRequisicoes = 10;

        System.out.println("\nConfiguração do teste:");
        System.out.println("Heap: " + tamanhoHeapKB + " KB");
        System.out.println("Tamanho mínimo: " + tamanhoMinimoBytes + " bytes");
        System.out.println("Tamanho máximo: " + tamanhoMaximoBytes + " bytes");
        System.out.println("Total de requisições: " + totalRequisicoes);

        Heap heap = new Heap(tamanhoHeapKB);
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        GerenciadorRequisicoes gerador = new GerenciadorRequisicoes(
                gerenciador,
                tamanhoMinimoBytes,
                tamanhoMaximoBytes,
                totalRequisicoes
        );

        gerador.executar();

        System.out.println("\nResultado esperado:");
        System.out.println("Requisições geradas: 10");
        System.out.println("Requisições alocadas: 0");
        System.out.println("Requisições falhadas: 10");
        System.out.println("Tamanho médio alocado: 0.00 bytes");
    }
}