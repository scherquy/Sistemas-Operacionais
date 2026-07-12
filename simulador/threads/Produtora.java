package simulador.threads;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Produtora implements Runnable {
    private final BlockingQueue<Requisicao> filaRequisicoes;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoesPorThread;
    private final int numeroInicial;
    private final int indiceInicial;
    private final int[] tamanhosRequisicoes;
    private final Random random;

    public Produtora(BlockingQueue<Requisicao> filaRequisicoes, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoesPorThread, int numeroInicial) {
        this.filaRequisicoes = filaRequisicoes;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoesPorThread = totalRequisicoesPorThread;
        this.numeroInicial = numeroInicial;
        this.indiceInicial = 0;
        this.tamanhosRequisicoes = null;
        this.random = new Random();
    }

    public Produtora(BlockingQueue<Requisicao> filaRequisicoes, int[] tamanhosRequisicoes, int indiceInicial, int totalRequisicoesPorThread, int numeroInicial) {
        this.filaRequisicoes = filaRequisicoes;
        this.tamanhoMinimoBytes = 0;
        this.tamanhoMaximoBytes = 0;
        this.totalRequisicoesPorThread = totalRequisicoesPorThread;
        this.numeroInicial = numeroInicial;
        this.indiceInicial = indiceInicial;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
        this.random = new Random();
    }

    @Override
    public void run() {
        for (int i = 0; i < totalRequisicoesPorThread; i++) {
            int tamanhoBytes;

            if (tamanhosRequisicoes == null) {
                tamanhoBytes = gerarTamanhoAleatorio();
            } else {
                tamanhoBytes = tamanhosRequisicoes[indiceInicial + i];
            }

            int numeroRequisicao = numeroInicial + i;

            Requisicao requisicao = new Requisicao(numeroRequisicao, tamanhoBytes);

            filaRequisicoes.add(requisicao);
        }
    }

    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;

        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }
}