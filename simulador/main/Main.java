package simulador.main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);

        System.out.printf("\n\n======================================");
        System.out.printf("\n   SIMULADOR DE GERENCIAMENTO HEAP");
        System.out.printf("\n======================================\n");

        System.out.printf("\nInforme o tamanho da heap em KB: ");
        int tamanhoHeapKB = entrada.nextInt();

        System.out.printf("\nInforme o tamanho mínimo das requisições em bytes: ");
        int tamanhoMinimoBytes = entrada.nextInt();

        System.out.printf("\nInforme o tamanho máximo das requisições em bytes: ");
        int tamanhoMaximoBytes = entrada.nextInt();

        System.out.printf("\nInforme o total de requisições: ");
        int totalRequisicoes = entrada.nextInt();

        if (tamanhoHeapKB <= 0) {
            System.out.printf("\nERRO. O tamanho da heap deve ser MAIOR que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMinimoBytes <= 0) {
            System.out.println("\nERRO. O tamanho mínimo deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        if (tamanhoMaximoBytes < tamanhoMinimoBytes) {
            System.out.println("\nERRO. O tamanho máximo deve ser maior ou igual ao tamanho mínimo\n\n");
            entrada.close();
            return;
        }

        if (totalRequisicoes <= 0) {
            System.out.println("\nERRO. O total de requisições deve ser maior que zero\n\n");
            entrada.close();
            return;
        }

        Sequencial simulador = new Sequencial(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes);

        simulador.executar();

        entrada.close();
    }
}