package simulador.algoritmos;

import simulador.heap.BlocoAlocado;
import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Compactacao {
    private final Heap heap; // heap do simulador
    private final GerenciadorHeap gerenciador; // classe base do simulador

    public Compactacao(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }

    public void compactar() {
        gerenciador.incrementaChamadasCompactacao(); // contador                                                                       

        int[] memoria = heap.getMemoria(); //cria uma referencia da heap para facilitar a manipulação
        ArrayList<BlocoAlocado> blocos = gerenciador.getTabelaAlocacoes();

        Collections.sort(blocos, Comparator.comparingInt(BlocoAlocado::getInicio));  // ordena os blocos pela posição em que inicia

        for (int i = 0; i < memoria.length; i++) { //zera a heap temporariamente 
            memoria[i] = 0;
        }

        int posicaoEscrita = 0; //para começar a escrevendo a partir da posição 0

        //percorre a lista de blocos ja ordenada
        for (BlocoAlocado bloco : blocos) {
            for (int j = 0; j < bloco.getTamanhoSlots(); j++) {
                memoria[posicaoEscrita + j] = bloco.getId(); //escreve o id na heap
            }

            gerenciador.atualizarInicioBloco(bloco.getId(), posicaoEscrita); //atualiza onde o bloco inicia na heap

            posicaoEscrita += bloco.getTamanhoSlots();  
        }
    }
}