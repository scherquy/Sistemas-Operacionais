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

    //recebe a lista de requisições
    public Paralelo(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes, int numThreadsProdutoras, int[] tamanhosRequisicoes) {
        this.tamanhoHeapKB = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.numThreadsProdutoras = numThreadsProdutoras;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
    }

    //executa a versão paralela e retorna as estatísticas
    public Estatisticas executar() throws InterruptedException {
        System.out.printf("\nSIMULADOR PARALELO\n");

        int numThreadsAlocadoras = numThreadsProdutoras; //define que o número de produtoras é igual ao de alocadoras

        System.out.printf("Heap: %d KB | Requisições Totais: %d | Produtoras: %d | Alocadoras: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, numThreadsProdutoras, numThreadsAlocadoras, tamanhoMinimoBytes, tamanhoMaximoBytes);

        Heap heap = new Heap(tamanhoHeapKB); //cria a heap
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap, numThreadsAlocadoras); //cria o gerenciador da heap, recebe o numero de alocadoras para dividir a heap em segmentos
        Estatisticas estatisticas = new Estatisticas(); // guarda as estatísticas

        BlockingQueue<Requisicao> filaRequisicoes = new LinkedBlockingQueue<>(); //cria a fila de requisições

        // listas para as threads produtoras e alocadoras
        ArrayList<Thread> threadsProdutoras = new ArrayList<>(); 
        ArrayList<Thread> threadsAlocadoras = new ArrayList<>();

        int requisicoesPorThread = totalRequisicoes / numThreadsProdutoras; //calcula quantas requisições cada produtora vai gerar
        int restoRequisicoes = totalRequisicoes % numThreadsProdutoras; //resto da divisão caso o calculo de cima não seja perfeito

        estatisticas.iniciarTempo(); //mede o tempo

        for (int i = 0; i < numThreadsAlocadoras; i++) { //cria as threads alocadoras
            Aloca alocaRunnable = new Aloca(filaRequisicoes, gerenciador, estatisticas); //cria um objeto com a lógica alocadora (recebe a fila, o gerenciador da heap e as estatísticas)

            Thread threadAlocadora = new Thread(alocaRunnable, "Thread-Alocadora-" + (i + 1)); //transforma o objeto criado em uma thread

            threadsAlocadoras.add(threadAlocadora); //guarda a thread na lista das alocadoras
            threadAlocadora.start(); //inicia a thread
        }

        //cria as threads produtoras
        int proximoNumeroRequisicao = 1; //define o número inicial da primeira requisição

        for (int i = 0; i < numThreadsProdutoras; i++) {
            int cargaTrabalho = requisicoesPorThread; //quantidade de requisições que essa produtora vai gerar

            if (i < restoRequisicoes) { //distribui o resto entre as produtoras
                cargaTrabalho++;
            }

            int numeroInicial = proximoNumeroRequisicao; //define o número da primeira requisição dessa produtora

            Produtora produtoraRunnable; //declara a produtora

            produtoraRunnable = new Produtora(filaRequisicoes, tamanhosRequisicoes, numeroInicial - 1, cargaTrabalho, numeroInicial); //se sim, usa essa lista

            proximoNumeroRequisicao += cargaTrabalho; //atualiza o número inicial da próxima produtora

            Thread threadProdutora = new Thread(produtoraRunnable, "Thread-Produtora-" + (i + 1)); //cria a thread produtora

            threadsProdutoras.add(threadProdutora); //bota a thread na lista das produtoras
            threadProdutora.start(); //inicia essa thread
        }

        // espera todas as produtoras terminarem
        for (Thread thread : threadsProdutoras) {
            thread.join();
        }

        // envia uma requisição de fim para cada alocadora
        for (int i = 0; i < numThreadsAlocadoras; i++) {
            filaRequisicoes.add(Requisicao.criarRequisicaoFim());
        }

        // espera todas as alocadoras terminarem
        for (Thread thread : threadsAlocadoras) {
            thread.join();
        }

        estatisticas.finalizarTempo(); // para o cronômetro
        estatisticas.imprimirResumo(gerenciador); //mostra as estatísticas da versão paralela

        return estatisticas; //retorna as estatísticas
    }
}