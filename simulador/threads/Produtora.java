package simulador.threads;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Produtora implements Runnable {

     // Fila compartilhada entre produtoras e alocadoras.
     // A produtora coloca requisições nessa fila.
     // As threads alocadoras retiram essas requisições da fila.
    private final BlockingQueue<Requisicao> filaRequisicoes;

     // Configurações das requisições
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoesPorThread;

     // Número inicial da primeira requisição gerada por esta produtora.
     // Isso evita que várias produtoras gerem logs com "Requisição 1",
     // "Requisição 2" etc. ao mesmo tempo.
    private final int numeroInicial;
    private final Random random;

     // o construtor é usado com BlockingQueue.
     // A produtora vai gerar tamanhos aleatórios, criar objetos Requisicao colocar essas requisições na fila.
     
    public Produtora(BlockingQueue<Requisicao> filaRequisicoes, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoesPorThread, int numeroInicial) {
        this.filaRequisicoes = filaRequisicoes;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoesPorThread = totalRequisicoesPorThread;
        this.numeroInicial = numeroInicial;
        this.random = new Random();
    }

     // é executado quando a thread começa.
    @Override
    public void run() {
        executarComFila();
    }

     // A produtora só gera requisições e coloca na fila.
     // Ela não acessa diretamente a heap.
    private void executarComFila() {
        String nomeThread = Thread.currentThread().getName();

        System.out.printf("\n[%s] Iniciada. Gerando %d requisições para a fila.\n", nomeThread, totalRequisicoesPorThread);

        for (int i = 0; i < totalRequisicoesPorThread; i++) {
            int tamanhoBytes = gerarTamanhoAleatorio();

             // Cada requisição recebe um número.
             // Se numeroInicial = 1, gera 1, 2, 3...
             // Se numeroInicial = 251, gera 251, 252, 253...
            int numeroRequisicao = numeroInicial + i;

            Requisicao requisicao = new Requisicao(numeroRequisicao, tamanhoBytes);

            try {
                 // put() coloca a requisição na fila.
                 // Se a fila estiver cheia, a thread espera até existir espaço. Isso evita perder requisições.
                filaRequisicoes.put(requisicao);

                System.out.printf("\n[%s] Enviou para a fila: %s", nomeThread, requisicao);

            } catch (InterruptedException e) {
                System.err.printf("\n[%s] Foi interrompida enquanto enviava requisição para a fila.\n", nomeThread);

                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("\n[%s] Finalizada. Todas as requisições foram enviadas.\n", nomeThread);
    }

     // Gera um tamanho aleatório dentro da faixa definida pelo usuário.
    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;
        
        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }
}