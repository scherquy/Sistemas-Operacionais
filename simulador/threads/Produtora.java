package simulador.threads;

import simulador.heap.GerenciadorHeap;
import simulador.estatisticas.Estatisticas;
import java.util.Random;

public class Produtora implements Runnable {
    
    private final GerenciadorHeap gerenciadorHeap;
    private final Estatisticas estatisticas; // Objeto global compartilhado para coletar dados de todas as threads
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoesPorThread;
    private final Random random;

    // Construtor para inicializar as configurações desta thread produtora
    public Produtora(GerenciadorHeap gerenciadorHeap, Estatisticas estatisticas, 
                     int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoesPorThread) {
        this.gerenciadorHeap = gerenciadorHeap;
        this.estatisticas = estatisticas;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoesPorThread = totalRequisicoesPorThread;
        this.random = new Random();
    }

    // O método run() define o ponto de entrada e o ciclo de vida da Thread
    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();
        System.out.printf("[%s] Iniciada. Gerando %d requisições...\n", nomeThread, totalRequisicoesPorThread);

        for (int i = 1; i <= totalRequisicoesPorThread; i++) {
            int tamanhoBytes = gerarTamanhoAleatorio();
            
            // Tenta realizar a alocação na Heap. 
            // Nota: futuramente, o método alocarFirstFit dentro do GerenciadorHeap 
            // precisará do Semáforo para proteger o vetor contra condições de corrida.
            int id = gerenciadorHeap.alocarFirstFit(tamanhoBytes);

            if (id != -1) {
                // Registra o sucesso nas estatísticas globais
                estatisticas.registrarRequisicaoAtendida(tamanhoBytes);
            } else {
                // Registra a falha se não houver espaço mesmo após liberação/compactação
                estatisticas.registrarRequisicaoFalhada();
                System.out.printf("[%s] Requisição %d FALHOU | Tamanho solicitado: %d bytes\n", nomeThread, i, tamanhoBytes);
            }

            // Opcional: Uma micropausa realista (ex: 10ms) para permitir que o escalonador do 
            // sistema operacional alterne a execução entre as diferentes Threads Produtoras
            try {
                Thread.sleep(10); 
            } catch (InterruptedException e) {
                System.out.printf("[%s] Interrompida inesperadamente.\n", nomeThread);
                break;
            }
        }

        System.out.printf("[%s] Finalizada. Concluiu suas requisições.\n", nomeThread);
    }

    // Lógica idêntica à que você implementou na versão sequencial
    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;
        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }
}