package simulador.algoritmos;

import simulador.heap.Heap;

public class FirstFit {
    private final Heap heap;

    public FirstFit(Heap heap) {
        this.heap = heap;
    }

    public int buscarEspaco(int tamanhoSlots) {
        return buscarEspaco(tamanhoSlots, 0, heap.getTotalSlots());
    }

    public int buscarEspaco(int tamanhoSlots, int inicioBusca, int fimBusca) {
        int[] memoria = heap.getMemoria();

        int inicioLivre = -1;
        int contadorLivre = 0;

        for (int i = inicioBusca; i < fimBusca; i++) {
            if (memoria[i] == 0) {
                if (inicioLivre == -1) {
                    inicioLivre = i;
                }

                contadorLivre++;

                if (contadorLivre >= tamanhoSlots) {
                    return inicioLivre;
                }
            } else {
                inicioLivre = -1;
                contadorLivre = 0;
            }
        }

        return -1;
    }
}