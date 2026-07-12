package simulador.threads;

import simulador.estatisticas.Estatisticas;
import simulador.heap.GerenciadorHeap;

import java.util.concurrent.BlockingQueue;

public class Aloca implements Runnable {
    private final BlockingQueue<Requisicao> filaRequisicoes;
    private final GerenciadorHeap gerenciadorHeap;
    private final Estatisticas estatisticas;

    public Aloca(BlockingQueue<Requisicao> filaRequisicoes, GerenciadorHeap gerenciadorHeap, Estatisticas estatisticas) {
        this.filaRequisicoes = filaRequisicoes;
        this.gerenciadorHeap = gerenciadorHeap;
        this.estatisticas = estatisticas;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Requisicao requisicao = filaRequisicoes.take();

                if (requisicao.isFim()) {
                    break;
                }

                int id = gerenciadorHeap.alocarFirstFit(requisicao.getTamanhoBytes());

                if (id != -1) {
                    estatisticas.registrarRequisicaoAtendida(requisicao.getTamanhoBytes());
                } else {
                    estatisticas.registrarRequisicaoFalhada();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}