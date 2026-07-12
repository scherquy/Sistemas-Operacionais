package simulador.estatisticas;

import simulador.heap.GerenciadorHeap;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Estatisticas {
    private final AtomicInteger totalRequisicoesGeradas;
    private final AtomicInteger requisicoesAtendidas;
    private final AtomicInteger requisicoesFalhadas;
    private final AtomicLong somaBytesAlocados;

    private int totalBlocosRemovidos;
    private int chamadasLiberacao;
    private int chamadasCompactacao;

    private long tempoInicio;
    private long tempoFim;

    public Estatisticas() {
        this.totalRequisicoesGeradas = new AtomicInteger(0);
        this.requisicoesAtendidas = new AtomicInteger(0);
        this.requisicoesFalhadas = new AtomicInteger(0);
        this.somaBytesAlocados = new AtomicLong(0);
        this.totalBlocosRemovidos = 0;
        this.chamadasLiberacao = 0;
        this.chamadasCompactacao = 0;
        this.tempoInicio = 0;
        this.tempoFim = 0;
    }

    public void iniciarTempo() {
        tempoInicio = System.nanoTime();
    }

    public void finalizarTempo() {
        tempoFim = System.nanoTime();
    }

    public void registrarRequisicaoAtendida(int tamanhoBytes) {
        totalRequisicoesGeradas.incrementAndGet();
        requisicoesAtendidas.incrementAndGet();
        somaBytesAlocados.addAndGet(tamanhoBytes);
    }

    public void registrarRequisicaoFalhada() {
        totalRequisicoesGeradas.incrementAndGet();
        requisicoesFalhadas.incrementAndGet();
    }

    public void capturarDadosGerenciador(GerenciadorHeap gerenciadorHeap) {
        totalBlocosRemovidos = gerenciadorHeap.getTotalBlocosLiberados();
        chamadasLiberacao = gerenciadorHeap.getTotalChamadasLiberacao();
        chamadasCompactacao = gerenciadorHeap.getTotalChamadasCompactacao();
    }

    public double getTempoTotalMs() {
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    public double getTamanhoMedioAlocadoBytes() {
        if (requisicoesAtendidas.get() == 0) {
            return 0;
        }

        return (double) somaBytesAlocados.get() / requisicoesAtendidas.get();
    }

    public void imprimirResumo(GerenciadorHeap gerenciadorHeap) {
        capturarDadosGerenciador(gerenciadorHeap);

        System.out.printf("\n\nRESUMO FINAL DA EXECUÇÃO");
        System.out.printf("\nTotal de Requisições Geradas: %d", totalRequisicoesGeradas.get());
        System.out.printf("\nRequisições Alocadas: %d", requisicoesAtendidas.get());
        System.out.printf("\nRequisições Falhadas: %d", requisicoesFalhadas.get());
        System.out.printf("\nSoma dos Bytes Alocados: %d bytes", somaBytesAlocados.get());
        System.out.printf("\nTamanho Médio Alocado: %.2f bytes", getTamanhoMedioAlocadoBytes());
        System.out.printf("\nTotal de Blocos Removidos: %d", totalBlocosRemovidos);
        System.out.printf("\nChamadas ao Algoritmo de Liberação: %d", chamadasLiberacao);
        System.out.printf("\nChamadas de Compactação: %d", chamadasCompactacao);
        System.out.printf("\nTempo Total de Execução: %.2f ms\n", getTempoTotalMs());
    }

    public int getTotalRequisicoesGeradas() {
        return totalRequisicoesGeradas.get();
    }

    public int getRequisicoesAtendidas() {
        return requisicoesAtendidas.get();
    }

    public int getRequisicoesFalhadas() {
        return requisicoesFalhadas.get();
    }

    public long getSomaBytesAlocados() {
        return somaBytesAlocados.get();
    }

    public int getTotalBlocosRemovidos() {
        return totalBlocosRemovidos;
    }

    public int getChamadasLiberacao() {
        return chamadasLiberacao;
    }

    public int getChamadasCompactacao() {
        return chamadasCompactacao;
    }
}