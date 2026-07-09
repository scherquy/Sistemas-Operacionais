package simulador.main;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;
import simulador.estatisticas.Estatisticas;
import simulador.threads.Aloca;
import simulador.threads.Produtora;
import simulador.threads.Requisicao;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Paralelo {

    private final int tamanhoHeapKB;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;
    private final int numThreadsProdutoras;

    public Paralelo(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int numThreadsProdutoras) {

        if (numThreadsProdutoras <= 0) {
            throw new IllegalArgumentException("O número de threads deve ser maior que zero");
        }

        this.tamanhoHeapKB = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.numThreadsProdutoras = numThreadsProdutoras;
    }

    public void executar() {
        System.out.printf("\nSIMULADOR PARALELO\n");

         // Para manter simples, usamos o mesmo número de threads produtoras e alocadoras.
         // Se o usuário escolher 4, teremos 4 produtoras e 4 alocadoras

        int numThreadsAlocadoras = numThreadsProdutoras;

        System.out.printf("Heap: %d KB | Requisições Totais: %d | Produtoras: %d | Alocadoras: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, numThreadsProdutoras, numThreadsAlocadoras, tamanhoMinimoBytes, tamanhoMaximoBytes);

        
         // Cria a heap e o gerenciador compartilhado.
         // Todas as threads alocadoras usam o mesmo gerenciador.    
        Heap heap = new Heap(tamanhoHeapKB);
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

         // Estatísticas compartilhadas entre as threads.
        Estatisticas estatisticas = new Estatisticas();

         // BlockingQueue é uma fila própria para uso com threads.
         // A produtora usa put() para inserir.
         // A alocadora usa take() para retirar.
         // Se a fila estiver cheia, put() espera.
         // Se a fila estiver vazia, take() espera.
        int capacidadeFila = 1000;

        if (totalRequisicoes < capacidadeFila) {
            capacidadeFila = totalRequisicoes;
        }

        if (capacidadeFila <= 0) {
            capacidadeFila = 1;
        }

        BlockingQueue<Requisicao> filaRequisicoes = new ArrayBlockingQueue<>(capacidadeFila);

        System.out.printf("\nCapacidade da fila de requisições: %d\n", capacidadeFila);

        ArrayList<Thread> threadsProdutoras = new ArrayList<>();
        ArrayList<Thread> threadsAlocadoras = new ArrayList<>();

         // Divide o total de requisições entre as produtoras.
        int requisicoesPorThread = totalRequisicoes / numThreadsProdutoras;
        int restoRequisicoes = totalRequisicoes % numThreadsProdutoras;

        estatisticas.iniciarTempo();

         // Primeiro iniciamos as alocadoras.
         // Elas ficam esperando requisições aparecerem na fila.
        for (int i = 0; i < numThreadsAlocadoras; i++) {
            Aloca alocaRunnable = new Aloca(filaRequisicoes, gerenciador, estatisticas);

            Thread threadAlocadora = new Thread(alocaRunnable, "Thread-Alocadora-" + (i + 1));
            threadsAlocadoras.add(threadAlocadora);
            threadAlocadora.start();
        }

         // Agora iniciamos as produtoras.
         // Elas geram requisições e colocam na fila.
        int proximoNumeroRequisicao = 1;

        for (int i = 0; i < numThreadsProdutoras; i++) {
            int cargaTrabalho = requisicoesPorThread;

            if (i < restoRequisicoes) {
                cargaTrabalho++;
            }

            // Cada produtora recebe um número inicial diferente.
            // Exemplo:
            // Produtora 1 começa na requisição 1
            // Produtora 2 começa depois da última requisição da produtora 1
            int numeroInicial = proximoNumeroRequisicao;

            Produtora produtoraRunnable = new Produtora(filaRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes, cargaTrabalho, numeroInicial);

            // Atualiza o próximo número inicial para a próxima produtora.
            proximoNumeroRequisicao += cargaTrabalho;

            Thread threadProdutora = new Thread(produtoraRunnable, "Thread-Produtora-" + (i + 1));
            threadsProdutoras.add(threadProdutora);
            threadProdutora.start();
        }

         // Espera todas as produtoras terminarem.
         // Quando isso acontecer, significa que todas as requisições já foram colocadas na fila.
        for (Thread thread : threadsProdutoras) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.printf("\nErro: a thread principal foi interrompida enquanto aguardava as produtoras.\n");
                Thread.currentThread().interrupt();
                return;
            }
        }

         // enviamos sinais de encerramento para as alocadoras.
         // enviamos uma requisição de fim para cada alocadora.
        for (int i = 0; i < numThreadsAlocadoras; i++) {
            try {
                filaRequisicoes.put(Requisicao.criarRequisicaoFim());
            } catch (InterruptedException e) {
                System.err.printf("\nErro ao enviar requisição de encerramento.\n");
                Thread.currentThread().interrupt();
                return;
            }
        }

         // Espera todas as alocadoras terminarem.
        for (Thread thread : threadsAlocadoras) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.printf("\nERRO.  thread principal foi interrompida enquanto aguardava as alocadoras.\n");
                Thread.currentThread().interrupt();
                return;
            }
        }

        estatisticas.finalizarTempo();
        estatisticas.imprimirResumo(gerenciador);
    }
}