package simulador.threads;

import simulador.estatisticas.Estatisticas;
import simulador.heap.GerenciadorHeap;

import java.util.concurrent.BlockingQueue;

public class Aloca implements Runnable {

     // Fila compartilhada entre produtoras e alocadoras.
     // As produtoras colocam requisições na fila.
     // As alocadoras retiram as requisições da fila.
    private final BlockingQueue<Requisicao> filaRequisicoes;

     // Gerenciador da heap.
     // Concentra as operações de alocação, liberação e compactação.
     // Concentra as operações de alocação, liberação e compactação.
     // O método alocarFirstFit() já usa semáforo para proteger a heap.
    private final GerenciadorHeap gerenciadorHeap;

     // Estatísticas compartilhadas entre as threads.
     // As estatísticas usam semáforo internamente para evitar problemas quando várias threads atualizam os contadores.
    private final Estatisticas estatisticas;

    public Aloca(BlockingQueue<Requisicao> filaRequisicoes, GerenciadorHeap gerenciadorHeap, Estatisticas estatisticas) {
        this.filaRequisicoes = filaRequisicoes;
        this.gerenciadorHeap = gerenciadorHeap;
        this.estatisticas = estatisticas;
    }

     // executado quando a thread alocadora começa.
    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();

        System.out.printf("\n[%s] Iniciada. Aguardando requisições da fila.\n", nomeThread);

         // A alocadora fica rodando até receber uma requisição especial de fim.
        while (true) {
            try {
                 // take() retira uma requisição da fila.
                 // Se a fila estiver vazia, a thread fica esperando.
                 // Isso é útil porque a produtora pode estar gerando requisições.
                Requisicao requisicao = filaRequisicoes.take();

                 // Se a requisição for de fim, a alocadora encerra.
                if (requisicao.isFim()) {
                    System.out.printf("\n[%s] Recebeu sinal de encerramento.\n", nomeThread);
                    break;
                }

                 // Tenta alocar a requisição na heap.
                 // A proteção da heap não fica aqui, ela fica dentro do GerenciadorHeap, no método alocarFirstFit().
                int id = gerenciadorHeap.alocarFirstFit(requisicao.getTamanhoBytes());

                if (id != -1) {
                    // Se conseguiu alocar, registra sucesso.
                    estatisticas.registrarRequisicaoAtendida(requisicao.getTamanhoBytes());

                    System.out.printf("\n[%s] Requisição %d alocada com sucesso | ID gerado: %d | Tamanho: %d bytes", nomeThread, requisicao.getNumero(), id, requisicao.getTamanhoBytes());

                }   else {
                     // Se não conseguiu alocar, registra falha.
                    estatisticas.registrarRequisicaoFalhada();

                    System.out.printf("\n[%s] Requisição %d FALHOU | Tamanho solicitado: %d bytes", nomeThread, requisicao.getNumero(), requisicao.getTamanhoBytes());
                }

            } catch (InterruptedException e) {
                System.err.printf("\n[%s] Foi interrompida enquanto aguardava requisição.\n", nomeThread);
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("\n[%s] Finalizada.\n", nomeThread);
    }
}