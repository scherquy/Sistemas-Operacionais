package simulador.algoritmos;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class FirstFit {
    private final Heap heap;
    private final GerenciadorHeap gerenciador;

    public FirstFit(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }

    public int buscarEspaco(int tamanhoSlots) {
        int[] memoria = heap.getMemoria(); // Obter a referência direta à memória da heap
        int inicioLivre = -1; 
        int contadorLivre = 0;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                // marca o inicio do bloco livre se ainda não tiver sido marcado
                if (inicioLivre == -1) inicioLivre = i;
                
                contadorLivre++;
                
                // encontrou espaço suficiente: retorna imediatamente (First Fit)
                if (contadorLivre >= tamanhoSlots) 
                return inicioLivre;
            } else {
                //bloco ocupado, reinicia a busca por um bloco livre
                inicioLivre = -1;
                contadorLivre = 0;
            }
        }

        return -1;
    }
   
     // tenta alocar um bloco na heap usando First Fit
    // retorna o ID do bloco alocado, ou -1 se não houver espaço
    public int alocar(int tamanhoBytes) {
        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);
        int inicio = buscarEspaco(tamanhoSlots);

        if (inicio == -1) {
            // sem espaço: aciona a liberação aleatória de pelo menos 30% da heap
            new Libera(heap, gerenciador).liberar();
            inicio = buscarEspaco(tamanhoSlots);

            if (inicio == -1) {
                // mesmo após liberação segue sem espaço contíguo (frag externa)
                new Compactacao(heap, gerenciador).compactar();
                // nova tentativa após a compactação
                inicio = buscarEspaco(tamanhoSlots);

                if (inicio == -1) {
                    System.out.printf("\nREQUISIÇÃO REJEITADA: sem espaço após liberação e compactação\n");
                    return -1;
                }
            }
        }
        // espaço encontrado: gera ID, escreve na heap e registra na tabela
        int id = gerenciador.gerarNovoId();
        heap.escrever(inicio, tamanhoSlots, id);
        gerenciador.registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);

        return id;
    }
}
