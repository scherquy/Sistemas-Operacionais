package simulador.algoritmos;

import simulador.heap.BlocoAlocado;
import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Compactacao {
    private final Heap heap;
    private final GerenciadorHeap gerenciador;

    public Compactacao(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }

    public void compactar() {
        gerenciador.incrementaChamadasCompactacao();

        int[] memoria = heap.getMemoria();
        ArrayList<BlocoAlocado> blocos = gerenciador.getTabelaAlocacoes();

        Collections.sort(blocos, Comparator.comparingInt(BlocoAlocado::getInicio));

        for (int i = 0; i < memoria.length; i++) {
            memoria[i] = 0;
        }

        int posicaoEscrita = 0;

        for (BlocoAlocado bloco : blocos) {
            for (int j = 0; j < bloco.getTamanhoSlots(); j++) {
                memoria[posicaoEscrita + j] = bloco.getId();
            }

            gerenciador.atualizarInicioBloco(bloco.getId(), posicaoEscrita);

            posicaoEscrita += bloco.getTamanhoSlots();
        }
    }
}