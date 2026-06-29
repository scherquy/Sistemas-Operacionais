package simulador.main;


import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;
import simulador.estatisticas.Estatisticas;
import simulador.threads.Produtora;
import java.util.ArrayList;


public class Paralelo {

    
// Parâmetros configurados pelo usuário (vindos da Main)
    private final int tamanhoHeapKB;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;

    // Definimos uma constante para determinar quantas threads vão simular os programas reais
    private static final int NUM_THREADS_PRODUTORAS = 4;


    public Paralelo(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes) {
        this.tamanhoHeapKB      = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes   = totalRequisicoes;
    }

    
    public void executar() {
        System.out.printf("\n======================================\n");
        System.out.printf("      SIMULADOR PARALELO / CONCORRENTE\n");
        System.out.printf("======================================\n");
        System.out.printf("Heap: %d KB | Requisições Totais: %d | Threads: %d | Faixa: %d a %d bytes\n", 
                tamanhoHeapKB, totalRequisicoes, NUM_THREADS_PRODUTORAS, tamanhoMinimoBytes, tamanhoMaximoBytes);

        // 1. Criamos a Heap e o Gerenciador ÚNICO. 
        // Todas as threads vão operar concorrentemente sobre este mesmo gerenciador.
        Heap heap = new Heap(tamanhoHeapKB);
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);

        // 2. Criamos o acumulador de estatísticas global.
        Estatisticas estatisticas = new Estatisticas();

        // 3. Divisão da carga de trabalho: repartimos o total de requisições entre as threads
        int requisicoesPorThread = totalRequisicoes / NUM_THREADS_PRODUTORAS;
        int restoRequisicoes = totalRequisicoes % NUM_THREADS_PRODUTORAS;

        ArrayList<Thread> threadsAtivas = new ArrayList<>();

        // Dispara o cronômetro de execução global
        estatisticas.iniciarTempo();

        // 4. Laço para criação e inicialização das Threads
        for (int i = 0; i < NUM_THREADS_PRODUTORAS; i++) {
            // Se a divisão não for exata, a última thread absorve o resto do trabalho
            int cargaTrabalho = requisicoesPorThread + (i == NUM_THREADS_PRODUTORAS - 1 ? restoRequisicoes : 0);

            // Passamos as mesmas referências (gerenciador e estatisticas) para todas as produtoras
            Produtora produtoraRunnable = new Produtora(
                gerenciador, 
                estatisticas, 
                tamanhoMinimoBytes, 
                tamanhoMaximoBytes, 
                cargaTrabalho
            );

            // Criamos o objeto encapsulador Thread do Java
            Thread threadProdutora = new Thread(produtoraRunnable, "Thread-Produtora-" + (i + 1));
            threadsAtivas.add(threadProdutora);

            // O método .start() sinaliza ao Sistema Operacional que a thread pode começar a rodar assincronamente
            threadProdutora.start();
        }

        // 5. Ponto de Sincronização (Barreira): 
        // A thread principal (main) precisa travar e esperar que todas as produtoras finalizem.
        for (Thread thread : threadsAtivas) {
            try {
                thread.join(); // Espera até que a thread em questão encerre o seu método run()
            } catch (InterruptedException e) {
                System.err.println("Erro: A thread principal de controle foi interrompida de forma inesperada.");
            }
        }

        // Para o cronômetro assim que a última thread produtora terminar o processamento
        estatisticas.finalizarTempo();

        // 6. Impressão dos Resultados Consolidados
        // O método imprimirResumo lerá os dados concorrentes já acumulados em 'estatisticas'
        estatisticas.imprimirResumo(gerenciador);
    }
}



