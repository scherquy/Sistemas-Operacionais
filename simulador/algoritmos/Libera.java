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
        gerenciador.incrementarChamadasLiberacao(); //contador

        ArrayList<BlocoAlocado> candidatos = gerenciador.getTabelaAlocacoes();

        if (candidatos.isEmpty()) { // se a tabela de alocação esta vazia
            return;
        }

        int metaSlots = (int) Math.ceil(heap.getTotalSlots() * 0.30); // calcular a meta de 30%
        int slotsLiberados = 0;

        Collections.shuffle(candidatos, new Random()); //Embaralha a tabela de alocações
       
        // percorre a tabela de alocação até atingir a meta de 30% ou ate terminar a tabela
        for (int i = 0; i < candidatos.size() && slotsLiberados < metaSlots; i++) {
            BlocoAlocado bloco = candidatos.get(i);  // pega um bloco da tabela

            boolean removeu = gerenciador.liberarPorIdInterno(bloco.getId()); //função para remover bloco pelo id

            if (removeu) { // se removeu incrementa o contador com o numero de slots liberados
                slotsLiberados += bloco.getTamanhoSlots();
            }
        }
    }
}