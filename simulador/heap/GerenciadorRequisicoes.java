package simulador.heap;

import simulador.estatisticas.Estatisticas;
import java.util.Random;

public class GerenciadorRequisicoes {
    private GerenciadorHeap gerenciadorHeap;
    private Random random;
    private Estatisticas estatisticas;

    // parâmetros configuráveis pelo usuário
    private int tamanhoMinimoBytes;
    private int tamanhoMaximoBytes;
    private int totalRequisicoes;

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
    }

    // gera um tamanho aleatório entre o mínimo e máximo configurados
    private int gerarTamanhoAleatorio() {
        int intervalo = tamanhoMaximoBytes - tamanhoMinimoBytes;
        return tamanhoMinimoBytes + random.nextInt(intervalo + 1);
    }

    // processa uma única requisição: gera o tamanho, tenta alocar e registra o resultado
    private void processarRequisicao(int numeroRequisicao) {
        int tamanhoBytes = gerarTamanhoAleatorio();
        int id = gerenciadorHeap.alocarFirstFit(tamanhoBytes);

        if (id != -1) {
            estatisticas.registrarRequisicaoAtendida(tamanhoBytes);
        }   else {
                estatisticas.registrarRequisicaoFalhada();
                System.out.printf("\nRequisição %d FALHOU | Tamanho solicitado: %d bytes\n", numeroRequisicao, tamanhoBytes);
        }
    }

    // executa todas as requisições de forma sequencial e mede o tempo total
    public void executar() {
        System.out.printf("\n=== Iniciando Gerador de Requisições ===");
        System.out.printf("\nTotal de requisições: %d | Faixa: %d a %d bytes\n", totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);

        estatisticas.iniciarTempo();

        for (int i = 1; i <= totalRequisicoes; i++) {
            processarRequisicao(i);
        }

        estatisticas.finalizarTempo();

        estatisticas.imprimirResumo(gerenciadorHeap);
    }
}