package simulador.main;
 
import simulador.heap.GerenciadorHeap;
import simulador.heap.GerenciadorRequisicoes;
import simulador.heap.Heap;
 
// a versao sequencial executa tudo em uma thread, vamos usar ela como base para comparar com a versao paralela
public class Sequencial {
 
    // parametros configurados pelo usuario
    private final int tamanhoHeapKB;
    private final int tamanhoMinimoBytes;
    private final int tamanhoMaximoBytes;
    private final int totalRequisicoes;
 
    public Sequencial(int tamanhoHeapKB, int tamanhoMinimoBytes, int tamanhoMaximoBytes, int totalRequisicoes){
        this.tamanhoHeapKB      = tamanhoHeapKB;
        this.tamanhoMinimoBytes = tamanhoMinimoBytes;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.totalRequisicoes   = totalRequisicoes;
    }
 
    public void executar() {
        System.out.printf("\n======================================\n");
        System.out.printf("      SIMULADOR SEQUENCIAL\n");
        System.out.printf("======================================\n");
        System.out.printf("Heap: %d KB | Requisições: %d | Faixa: %d a %d bytes\n", tamanhoHeapKB, totalRequisicoes, tamanhoMinimoBytes, tamanhoMaximoBytes);
 
        Heap heap = new Heap(tamanhoHeapKB);
 
        // gerenciador que coordena alocacao, liberacao e compactacao
        GerenciadorHeap gerenciador = new GerenciadorHeap(heap);
 
        // cria o gerador de requisicoes e executa o loop sequencial
        GerenciadorRequisicoes gerador = new GerenciadorRequisicoes(gerenciador, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes);
 
        gerador.executar();
    }
}
