package simulador.algoritmos;

import simulador.heap.BlocoAlocado;
import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Libera {
    private final Heap heap;
    private final GerenciadorHeap gerenciador;

    public Libera(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }

    public void liberar() {
        gerenciador.incrementarChamadasLiberacao();

        ArrayList<BlocoAlocado> candidatos = gerenciador.getTabelaAlocacoes();

        if (candidatos.isEmpty()) {
            return;
        }

        int metaSlots = (int) Math.ceil(heap.getTotalSlots() * 0.30);
        int slotsLiberados = 0;

        Collections.shuffle(candidatos, new Random());

        for (int i = 0; i < candidatos.size() && slotsLiberados < metaSlots; i++) {
            BlocoAlocado bloco = candidatos.get(i);

            boolean removeu = gerenciador.liberarPorIdInterno(bloco.getId());

            if (removeu) {
                slotsLiberados += bloco.getTamanhoSlots();
            }
        }
    }
}