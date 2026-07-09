package simulador.main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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
            System.out.printf("\n\nERRO. O tamanho da heap deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMinimoBytes <= 0) {
            System.out.printf("\n\nERRO. O tamanho mínimo deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMaximoBytes < tamanhoMinimoBytes) {
            System.out.printf("\n\nERRO. O tamanho máximo deve ser maior ou igual ao tamanho mínimo\n\n");
            entrada.close();
            return;
        }

        if (totalRequisicoes <= 0) {
            System.out.printf("\n\nERRO. O total de requisições deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        System.out.printf("\n\n1. Versão Sequencial");
        System.out.printf("\n2. Versão Paralela");
        System.out.printf("\n\nEscolha a versão a ser executada (1 ou 2): ");
        int escolhaVersao = entrada.nextInt();

        if (escolhaVersao == 1) {
            Sequencial simulador = new Sequencial(
                tamanhoHeapKB,
                tamanhoMinimoBytes,
                tamanhoMaximoBytes,
                totalRequisicoes
            );

            simulador.executar();

        }   else if (escolhaVersao == 2) {
                System.out.printf("\nInforme o número de threads(2, 4 ou 8): ");
                int numThreadsProdutoras = entrada.nextInt();

                /*
                * Para os testes do trabalho, vamos aceitar apenas 2, 4 ou 8 threads.

                * Isso ajuda a manter os testes padronizados e facilita a comparação
                * entre os resultados.
                */
                if (numThreadsProdutoras != 2 && numThreadsProdutoras != 4 && numThreadsProdutoras != 8) {
                    System.out.printf("\nERRO. O número de threads deve ser 2, 4 ou 8.\n\n");
                    entrada.close();
                    return;
                }

                Paralelo simulador = new Paralelo(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, numThreadsProdutoras);

                simulador.executar();
            }   else {
                    System.out.printf("\n\nERRO. Opção inválida. Escolha 1 ou 2.\n\n");
                }

        entrada.close();
    }
}