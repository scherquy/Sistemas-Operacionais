/*package simulador.heap;

import simulador.estatisticas.Estatisticas;

import java.util.Random;
//Classe que era utilizada na versão sequencial do projeto
public class GerenciadorRequisicoes {
    private final GerenciadorHeap gerenciadorHeap;
    private final Random random;
    private final Estatisticas estatisticas;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;
    private final int[] tamanhosRequisicoes;

    public GerenciadorRequisicoes(GerenciadorHeap gerenciadorHeap, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes) {
        if (tamanhoMinimoBytes <= 0) {
            throw new IllegalArgumentException("O tamanho mínimo deve ser maior que zero.");
        }

        if (tamanhoMaximoBytes < tamanhoMinimoBytes) {
            throw new IllegalArgumentException("O tamanho máximo deve ser maior ou igual ao mínimo.");
        }

        if (totalRequisicoes <= 0) {
            throw new IllegalArgumentException("O total de requisições deve ser maior que zero.");
        }

        this.gerenciadorHeap = gerenciadorHeap;
        this.random = new Random();
        this.estatisticas = new Estatisticas();
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes = totalRequisicoes;
        this.tamanhosRequisicoes = null;
    }

    public GerenciadorRequisicoes(GerenciadorHeap gerenciadorHeap, int[] tamanhosRequisicoes) {
        this.gerenciadorHeap = gerenciadorHeap;
        this.random = new Random();
        this.estatisticas = new Estatisticas();
        this.tamanhoMinimoBytes = 0;
        this.tamanhoMaximoBytes = 0;
        this.totalRequisicoes = tamanhosRequisicoes.length;
        this.tamanhosRequisicoes = tamanhosRequisicoes;
    }

    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;

        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }

    private void processarRequisicao(int tamanhoBytes) {
        int id = gerenciadorHeap.alocarFirstFit(tamanhoBytes);

        if (id != -1) {
            estatisticas.registrarRequisicaoAtendida(tamanhoBytes);
        } else {
            estatisticas.registrarRequisicaoFalhada();
        }
    }

    public Estatisticas executar() {
        estatisticas.iniciarTempo();

        if (tamanhosRequisicoes == null) {
            for (int i = 1; i <= totalRequisicoes; i++) {
                int tamanhoBytes = gerarTamanhoAleatorio();
                processarRequisicao(tamanhoBytes);
            }
        } else {
            for (int i = 0; i < tamanhosRequisicoes.length; i++) {
                processarRequisicao(tamanhosRequisicoes[i]);
            }
        }

        estatisticas.finalizarTempo();
        estatisticas.imprimirResumo(gerenciadorHeap);

        return estatisticas;
    }
} */