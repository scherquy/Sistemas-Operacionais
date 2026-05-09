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

        ArrayList<BlocoAlocado> tabela = gerenciador.getTabelaAlocacoes();

        if (tabela.isEmpty()) {
            System.out.println("Tabela vazia, nada para liberar");
            return;
        }

        int metaSlots = (int) Math.ceil(heap.getTotalSlots() * 0.30);
        int slotsLiberados = 0;

        System.out.printf("\nINICIANDO LIBERAÇÃO: %d slots (%d bytes)\n", metaSlots, metaSlots * 4);

        ArrayList<BlocoAlocado> candidatos = new ArrayList<>(tabela);
        Collections.shuffle(candidatos, new Random());

        for (int i = 0; i < candidatos.size() && slotsLiberados < metaSlots; i++) {
            BlocoAlocado bloco = candidatos.get(i);

            boolean removeu = gerenciador.liberarPorId(bloco.getId());

            if (removeu) {
                slotsLiberados += bloco.getTamanhoSlots();

                System.out.printf("\nID Removido = %d | Slots = %d | Liberado Até Agora = %d/%d\n", bloco.getId(), bloco.getTamanhoSlots(), slotsLiberados, metaSlots);
            }
        }

        if (slotsLiberados < metaSlots) {
            System.out.printf("\nMeta não atingida\nLiberado = %d | Meta = %d\nBlocos Insuficientes\n", slotsLiberados, metaSlots);
        } else {
            System.out.printf("\nLiberação Concluída.\nTotal liberado = %d slots (%d bytes)\n", slotsLiberados, slotsLiberados * 4);
        }
    }
}
