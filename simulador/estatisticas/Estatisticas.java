package simulador.estatisticas;

import simulador.heap.GerenciadorHeap;
import java.util.concurrent.Semaphore;

public class Estatisticas {
    private int totalRequisicoesGeradas;
    private int requisicoesAtendidas;
    private int requisicoesFalhadas;
    private long somaBytesAlocados;
    private long tempoInicio;
    private long tempoFim;

    private final Semaphore mutex = new Semaphore(1);

    public Estatisticas() {
        this.totalRequisicoesGeradas = 0;
        this.requisicoesAtendidas = 0;
        this.requisicoesFalhadas = 0;
        this.somaBytesAlocados = 0;
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
        try{
            mutex.acquire();
            totalRequisicoesGeradas++;
            requisicoesAtendidas++;
            somaBytesAlocados += tamanhoBytes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("A thread foi interrompida enquanto registrava uma requisição atendida");
        } finally {
            mutex.release();
        }
    }

    public void registrarRequisicaoFalhada() {
        try{
            mutex.acquire();
            totalRequisicoesGeradas++;
            requisicoesFalhadas++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("A thread foi interrompida enquanto registrava uma requisição falhada");
        } finally {
            mutex.release();
        }
    }

    public double getTempoTotalMs() {
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    public double getTamanhoMedioAlocadoBytes() {
        if (requisicoesAtendidas == 0) {
            return 0;
        }

        return (double) somaBytesAlocados / requisicoesAtendidas;
    }

    public void imprimirResumo(GerenciadorHeap gerenciadorHeap) {
        System.out.printf("\n\n======================================");
        System.out.printf("\n       RESUMO FINAL DA EXECUÇÃO");
        System.out.printf("\n======================================");
        System.out.printf("\nTotal de Requisições Geradas: %d", totalRequisicoesGeradas);
        System.out.printf("\nRequisições Alocadas: %d", requisicoesAtendidas);
        System.out.printf("\nRequisições Falhadas: %d", requisicoesFalhadas);
        System.out.printf("\nSoma dos Bytes Alocados: %d bytes", somaBytesAlocados);
        System.out.printf("\nTamanho Médio Alocado: %.2f bytes", getTamanhoMedioAlocadoBytes());
        System.out.printf("\nTotal de Blocos Removidos: %d", gerenciadorHeap.getTotalBlocosLiberados());
        System.out.printf("\nChamadas ao Algoritmo de Liberação: %d", gerenciadorHeap.getTotalChamadasLiberacao());
        System.out.printf("\nChamadas de Compactação: %d", gerenciadorHeap.getTotalChamadasCompactacao());
        System.out.printf("\nTempo Total de Execução: %.2f ms", getTempoTotalMs());
        System.out.printf("\n\n======================================\n");
    }

    public int getTotalRequisicoesGeradas() {
        return totalRequisicoesGeradas;
    }

    public int getRequisicoesAtendidas() {
        return requisicoesAtendidas;
    }

    public int getRequisicoesFalhadas() {
        return requisicoesFalhadas;
    }

    public long getSomaBytesAlocados() {
        return somaBytesAlocados;
    }
}