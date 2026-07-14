package simulador.threads;

import simulador.estatisticas.Estatisticas;
import simulador.heap.GerenciadorHeap;

import java.util.concurrent.BlockingQueue;

public class Aloca implements Runnable { //implements Runnable permite executar essa classe como uma thread
    private final BlockingQueue<Requisicao> filaRequisicoes;
    private final GerenciadorHeap gerenciadorHeap;
    private final Estatisticas estatisticas;

    public Aloca(BlockingQueue<Requisicao> filaRequisicoes, GerenciadorHeap gerenciadorHeap, Estatisticas estatisticas) {
        this.filaRequisicoes = filaRequisicoes;
        this.gerenciadorHeap = gerenciadorHeap;
        this.estatisticas = estatisticas;
    }

    //método executado quando a thread é iniciada
    @Override
    public void run() {
        while (true) { // a alocadora fica tirando requisições da fila até receber uma requisição de fim
            try { //utilizamos porque o take() pode lançar uma exceção
                Requisicao requisicao = filaRequisicoes.take(); //retira requisições da fila e se a fila estiver vazia a thread espera ter algo na fila

                //se for uma requisição de encerramento, sai do loop
                if (requisicao.isFim()) {
                    break;
                }

                int id = gerenciadorHeap.alocarFirstFit(requisicao.getTamanhoBytes()); //chama o gerenciador da heap para alocar o tamanho solicitado

                if (id != -1) { //(ID != -1) == alocação deu certo
                    estatisticas.registrarRequisicaoAtendida(requisicao.getTamanhoBytes()); //registra uma requisição atendida
                } else {
                    estatisticas.registrarRequisicaoFalhada(); //(ID == -1) == alocação deu errado, registra falha
                }

            } catch (InterruptedException e) { //caso a thread seja interrompida enquanto espera na fila
                Thread.currentThread().interrupt(); // marca a interrupção da thread
                break; //sai do loop
            }
        }
    }
}