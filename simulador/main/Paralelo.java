package simulador.main;

import simulador.estatisticas.Estatisticas;
import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;
import simulador.threads.Aloca;
import simulador.threads.Produtora;
import simulador.threads.Requisicao;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Paralelo {
    private final int tamanhoHeapKB;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;
    private final int numThreadsProdutoras;
    private final int[] tamanhosRequisicoes;

    public Paralelo(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int numThreadsProdutoras) {
        this(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, numThreadsProdutoras, null);
    }

    public Paralelo(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int numThreadsProdutoras, int[] tamanhosRequisicoes) {
        this.tamanhoHeapKB = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.numThreadsProdutoras = numThreadsProdutoras;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
    }

    public Estatisticas executar() throws InterruptedException {
        System.out.printf("\nSIMULADOR PARALELO\n");

        int numThreadsAlocadoras = numThreadsProdutoras;

        System.out.printf("Heap: %d KB | Requisições Totais: %d | Produtoras: %d | Alocadoras: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, numThreadsProdutoras, numThreadsAlocadoras, tamanhoMinimoBytes, tamanhoMaximoBytes);

        Heap heap = new Heap(tamanhoHeapKB);
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap, numThreadsAlocadoras);
        Estatisticas estatisticas = new Estatisticas();

        BlockingQueue<Requisicao> filaRequisicoes = new LinkedBlockingQueue<>();

        ArrayList<Thread> threadsProdutoras = new ArrayList<>();
        ArrayList<Thread> threadsAlocadoras = new ArrayList<>();

        int requisicoesPorThread = totalRequisicoes / numThreadsProdutoras;
        int restoRequisicoes = totalRequisicoes % numThreadsProdutoras;

        estatisticas.iniciarTempo();

        for (int i = 0; i < numThreadsAlocadoras; i++) {
            Aloca alocaRunnable = new Aloca(filaRequisicoes, gerenciador, estatisticas);

            Thread threadAlocadora = new Thread(alocaRunnable, "Thread-Alocadora-" + (i + 1));

            threadsAlocadoras.add(threadAlocadora);
            threadAlocadora.start();
        }

        int proximoNumeroRequisicao = 1;

        for (int i = 0; i < numThreadsProdutoras; i++) {
            int cargaTrabalho = requisicoesPorThread;

            if (i < restoRequisicoes) {
                cargaTrabalho++;
            }

            int numeroInicial = proximoNumeroRequisicao;

            Produtora produtoraRunnable;

            if (tamanhosRequisicoes == null) {
                produtoraRunnable = new Produtora(filaRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes, cargaTrabalho, numeroInicial);
            } else {
                produtoraRunnable = new Produtora(filaRequisicoes, tamanhosRequisicoes, numeroInicial - 1, cargaTrabalho, numeroInicial);
            }

            proximoNumeroRequisicao += cargaTrabalho;

            Thread threadProdutora = new Thread(produtoraRunnable, "Thread-Produtora-" + (i + 1));

            threadsProdutoras.add(threadProdutora);
            threadProdutora.start();
        }

        for (Thread thread : threadsProdutoras) {
            thread.join();
        }

        for (int i = 0; i < numThreadsAlocadoras; i++) {
            filaRequisicoes.add(Requisicao.criarRequisicaoFim());
        }

        for (Thread thread : threadsAlocadoras) {
            thread.join();
        }

        estatisticas.finalizarTempo();
        estatisticas.imprimirResumo(gerenciador);

        return estatisticas;
    }
}