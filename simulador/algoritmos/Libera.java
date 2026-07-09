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

     // remove blocos aleatórios da heap até liberar pelo menos 30% do tamanho total da heap.
     // altera a heap e a tabela de alocações.
     // na versão paralela ele deve ser executado dentro de uma seção crítica protegida por semáforo.

    public void liberar() {
        gerenciador.incrementarChamadasLiberacao();

        ArrayList<BlocoAlocado> tabela = gerenciador.getTabelaAlocacoes();

        if (tabela.isEmpty()) {
            System.out.printf("\nTabela vazia, nada para liberar.\n");
            return;
        }

        int metaSlots = (int) Math.ceil(heap.getTotalSlots() * 0.30);
        int slotsLiberados = 0;

        System.out.printf("\nINICIANDO LIBERAÇÃO RANDOM: meta = %d slots (%d bytes)\n", metaSlots, metaSlots * 4);

         // Criamos uma cópia da tabela antes de embaralhar.
         // Isso não bagunça a tabela original enquanto escolhemos os blocos para remoção
        ArrayList<BlocoAlocado> candidatos = new ArrayList<>(tabela);

         // Embaralha a lista de candidatos.
         // Isso faz a liberação funcionar com lógica RANDOM porque a ordem dos blocos removidos é aleatória.
        Collections.shuffle(candidatos, new Random());

        for (int i = 0; i < candidatos.size() && slotsLiberados < metaSlots; i++) {
            BlocoAlocado bloco = candidatos.get(i);

           // Remove o bloco da heap e da tabela de alocações.
           // Aqui usamos liberarPorIdInterno() porque a liberação RANDOM já deve estar sendo executada dentro da seção crítica da heap.
            boolean removeu = gerenciador.liberarPorIdInterno(bloco.getId());

            if (removeu) {
                slotsLiberados += bloco.getTamanhoSlots();

                System.out.printf("\nID removido = %d | Slots = %d | Liberado até agora = %d/%d", bloco.getId(), bloco.getTamanhoSlots(), slotsLiberados, metaSlots);
            }
        }

        if (slotsLiberados < metaSlots) {
            System.out.printf("\nMeta não atingida. Liberado = %d slots | Meta = %d slots | Blocos insuficientes.\n", slotsLiberados, metaSlots);
        }   else {
                System.out.printf("\nLiberação concluída. Total liberado = %d slots (%d bytes)\n", slotsLiberados, slotsLiberados * 4);
            }
    }
}