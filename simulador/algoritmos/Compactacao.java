package simulador.algoritmos;

import simulador.heap.BlocoAlocado;
import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class Compactacao {
    private final Heap heap;
    private final GerenciadorHeap gerenciador;

    public Compactacao(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }


    // compacta a heap movendo todos os blocos ocupados para o início,
    // eliminando os buracos causados pela fragmentação externa.
    // deve ser chamada após a liberação aleatória, quando ainda não
    // há espaço contíguo suficiente para a nova alocação.

    public void compactar() {
        gerenciador.incrementaChamadasCompactacao();
        int[] memoria = heap.getMemoria();
        int posicaoEscrita = 0;

        System.out.printf("\nINICIANDO COMPACTAÇÃO\n");

        //percorre toda heap slot por slot
        for (int i = 0; i < memoria.length; i++) {
           
           // slot ocupado — precisa ser movido para mais perto do início
            if (memoria[i] != 0) {
                int idBloco = memoria[i];

                // só processa o primeiro slot de cada bloco
                // os demais slots do mesmo bloco serão copiados no loop interno
                BlocoAlocado bloco = gerenciador.buscarBlocoPorId(idBloco);
               
                //ignora se o bloco não está na tabela
                if (bloco == null) continue;

                int inicioOriginal = bloco.getInicio();

                if (inicioOriginal != posicaoEscrita) {
                    for (int j = 0; j < bloco.getTamanhoSlots(); j++) {
                        memoria[posicaoEscrita + j] = idBloco;
                    }
                    
                    // apaga os slots antigos do bloco
                    for (int j = posicaoEscrita + bloco.getTamanhoSlots(); j < inicioOriginal + bloco.getTamanhoSlots(); j++) {
                        if (j >= 0 && j < memoria.length) memoria[j] = 0;
                    }

                    System.out.printf("Bloco ID = %d movido: %d -> %d\n", idBloco, inicioOriginal, posicaoEscrita);
                    
                    // atualiza o início do bloco na tabela para refletir a nova posição
                    bloco.setInicio(posicaoEscrita);
                }
                    
                // avança a posição de escrita pelo tamanho do bloco
                posicaoEscrita += bloco.getTamanhoSlots();

                // usa inicioOriginal para pular corretamente que já foi atualizado para a nova posição
                i = inicioOriginal + bloco.getTamanhoSlots() - 1;
            }
        }
           
        // zera tudo depois do ultimo bloco ocupado
        for (int x = posicaoEscrita; x < memoria.length; x++) {
            memoria[x] = 0;
        }

        System.out.printf("COMPACTAÇÃO CONCLUÍDA. Espaço livre a partir do slot %d\n", posicaoEscrita);
    }
}
