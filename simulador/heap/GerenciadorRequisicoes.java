package simulador.heap;

import java.util.Random;

public class GerenciadorRequisicoes {
    private GerenciadorHeap gerenciadorHeap;
    private Random random;

    // parâmetros configuráveis pelo usuário
    private int tamanhoMinimoBytes;
    private int tamanhoMaximoBytes;
    private int totalRequisicoes;

    // estatísticas coletadas durante a execução
    private int requisoesAtendidas;
    private int requisoesFalhadas;
    private long somaBytesSolicitados;

    public GerenciadorRequisicoes(GerenciadorHeap gerenciadorHeap,
                                   int tamanhoMinimoBytes,
                                   int tamanhoMaximoBytes,
                                   int totalRequisicoes) {

        if (tamanhoMinimoBytes <= 0) {
            throw new IllegalArgumentException("O tamanho mínimo deve ser maior que zero.");
        }

        if (tamanhoMaximoBytes < tamanhoMinimoBytes) {
            throw new IllegalArgumentException("O tamanho máximo deve ser maior ou igual ao mínimo.");
        }

        if (totalRequisicoes <= 0) {
            throw new IllegalArgumentException("O total de requisições deve ser maior que zero.");
        }

        this.gerenciadorHeap = gerenciadorHeap;
        this.random = new Random();
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.requisoesAtendidas = 0;
        this.requisoesFalhadas = 0;
        this.somaBytesSolicitados = 0;
    }

    // gera um tamanho aleatório entre o mínimo e máximo configurados
    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;
        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }

    // processa uma única requisição: gera o tamanho, tenta alocar e registra o resultado
    private void processarRequisicao(int numeroRequisicao) {
        int tamanhoBytes = gerarTamanhoAleatorio();
        somaBytesSolicitados += tamanhoBytes;

        int id = gerenciadorHeap.alocarFirstFit(tamanhoBytes);

        if (id != -1) {
            requisoesAtendidas++;
        } else {
            // falha mesmo após liberação e compactação: heap sem capacidade para essa requisição
            requisoesFalhadas++;
            System.out.printf("[Requisição %d] FALHOU | Tamanho solicitado: %d bytes%n",
                    numeroRequisicao, tamanhoBytes);
        }
    }

    // executa todas as requisições de forma sequencial e mede o tempo total
    public void executar() {
        System.out.println("\n=== Iniciando Gerador de Requisições ===");
        System.out.printf("Total de requisições: %d | Faixa: %d a %d bytes%n",
                totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);

        long inicio = System.currentTimeMillis();

        for (int i = 1; i <= totalRequisicoes; i++) {
            processarRequisicao(i);
        }

        long fim = System.currentTimeMillis();
        long tempoTotalMs = fim - inicio;

        imprimirResumoFinal(tempoTotalMs);
    }

    // exibe o resumo completo conforme exigido pela especificação
    private void imprimirResumoFinal(long tempoTotalMs) {
        double tamanhoMedioBytes = totalRequisicoes > 0
                ? (double) somaBytesSolicitados / totalRequisicoes
                : 0;

        System.out.println("\n======================================");
        System.out.println("       RESUMO FINAL DA EXECUÇÃO");
        System.out.println("======================================");
        System.out.printf("Total de requisições geradas:       %d%n", totalRequisicoes);
        System.out.printf("Requisições atendidas (alocadas):   %d%n", requisoesAtendidas);
        System.out.printf("Requisições falhadas:               %d%n", requisoesFalhadas);
        System.out.printf("Tamanho médio solicitado:           %.2f bytes%n", tamanhoMedioBytes);
        System.out.printf("Total de blocos removidos:          %d%n", gerenciadorHeap.getTotalBlocosLiberados());
        System.out.printf("Chamadas ao algoritmo de liberação: %d%n", gerenciadorHeap.getTotalChamadasLiberacao());
        System.out.printf("Tempo total de execução:            %d ms%n", tempoTotalMs);
        System.out.println("======================================");
    }

    public int getRequisoesAtendidas() {
        return requisoesAtendidas;
    }

    public int getRequisoesFalhadas() {
        return requisoesFalhadas;
    }

    public long getSomaBytesSolicitados() {
        return somaBytesSolicitados;
    }
}