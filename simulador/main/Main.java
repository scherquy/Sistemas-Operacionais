package simulador.main;

import simulador.estatisticas.Estatisticas;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner entrada = new Scanner(System.in);

        System.out.printf("\nSIMULADOR DE GERENCIAMENTO HEAP");

        System.out.printf("\n\nInforme o tamanho da heap em KB: ");
        int tamanhoHeapKB = entrada.nextInt();

        System.out.printf("\nInforme o tamanho mínimo das requisições em bytes: ");
        int tamanhoMinimoBytes = entrada.nextInt();

        System.out.printf("\nInforme o tamanho máximo das requisições em bytes: ");
        int tamanhoMaximoBytes = entrada.nextInt();

        System.out.printf("\nInforme o total de requisições: ");
        int totalRequisicoes = entrada.nextInt();

        if (tamanhoHeapKB <= 0) {
            System.out.printf("\n\nO tamanho da heap deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMinimoBytes <= 0) {
            System.out.printf("\n\nO tamanho mínimo deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMaximoBytes < tamanhoMinimoBytes) {
            System.out.printf("\n\nO tamanho máximo deve ser maior ou igual ao tamanho mínimo\n\n");
            entrada.close();
            return;
        }

        if (totalRequisicoes <= 0) {
            System.out.printf("\n\nO total de requisições deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        System.out.printf("\nInforme o número de threads da versão paralela (2, 4 ou 8): ");
        int numThreadsProdutoras = entrada.nextInt();

        if (numThreadsProdutoras != 2 && numThreadsProdutoras != 4 && numThreadsProdutoras != 8) {
            System.out.printf("\nO número de threads deve ser 2, 4 ou 8.\n\n");
            entrada.close();
            return;
        }

        int[] tamanhosRequisicoes = gerarRequisicoes(totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);

        Sequencial sequencial = new Sequencial(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, tamanhosRequisicoes);

        Estatisticas estatisticasSequencial = sequencial.executar();

        Paralelo paralelo = new Paralelo(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, numThreadsProdutoras, tamanhosRequisicoes);

        Estatisticas estatisticasParalelo = paralelo.executar();

        imprimirComparacao(estatisticasSequencial, estatisticasParalelo);

        entrada.close();
    }

    private static int[] gerarRequisicoes(int totalRequisicoes, int tamanhoMinimoBytes, int tamanhoMaximoBytes) {
        int[] tamanhos = new int[totalRequisicoes];
        Random random = new Random(12345);

        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;

        for (int i = 0; i < totalRequisicoes; i++) {
            tamanhos[i] = tamanhoMinimoBytes + random.nextInt(intervalo + 1);
        }

        return tamanhos;
    }

    private static void imprimirComparacao(Estatisticas sequencial, Estatisticas paralelo) {
        double tempoSequencial = sequencial.getTempoTotalMs();
        double tempoParalelo = paralelo.getTempoTotalMs();

        System.out.printf("\n\nCOMPARAÇÃO FINAL ENTRE AS VERSÕES");

        System.out.printf("\n\nTotal de Requisições Geradas");
        System.out.printf("\nSequencial: %d", sequencial.getTotalRequisicoesGeradas());
        System.out.printf("\nParalela:   %d", paralelo.getTotalRequisicoesGeradas());

        System.out.printf("\n\nRequisições Alocadas");
        System.out.printf("\nSequencial: %d", sequencial.getRequisicoesAtendidas());
        System.out.printf("\nParalela:   %d", paralelo.getRequisicoesAtendidas());

        System.out.printf("\n\nRequisições Falhadas");
        System.out.printf("\nSequencial: %d", sequencial.getRequisicoesFalhadas());
        System.out.printf("\nParalela:   %d", paralelo.getRequisicoesFalhadas());

        System.out.printf("\n\nSoma dos Bytes Alocados");
        System.out.printf("\nSequencial: %d bytes", sequencial.getSomaBytesAlocados());
        System.out.printf("\nParalela:   %d bytes", paralelo.getSomaBytesAlocados());

        System.out.printf("\n\nTamanho Médio Alocado");
        System.out.printf("\nSequencial: %.2f bytes", sequencial.getTamanhoMedioAlocadoBytes());
        System.out.printf("\nParalela:   %.2f bytes", paralelo.getTamanhoMedioAlocadoBytes());

        System.out.printf("\n\nTotal de Blocos Removidos");
        System.out.printf("\nSequencial: %d", sequencial.getTotalBlocosRemovidos());
        System.out.printf("\nParalela:   %d", paralelo.getTotalBlocosRemovidos());

        System.out.printf("\n\nChamadas ao Algoritmo de Liberação");
        System.out.printf("\nSequencial: %d", sequencial.getChamadasLiberacao());
        System.out.printf("\nParalela:   %d", paralelo.getChamadasLiberacao());

        System.out.printf("\n\nChamadas de Compactação");
        System.out.printf("\nSequencial: %d", sequencial.getChamadasCompactacao());
        System.out.printf("\nParalela:   %d", paralelo.getChamadasCompactacao());

        System.out.printf("\n\nTempo Total de Execução");
        System.out.printf("\nSequencial: %.2f ms", tempoSequencial);
        System.out.printf("\nParalela:   %.2f ms", tempoParalelo);

        System.out.printf("\n");
    }
}