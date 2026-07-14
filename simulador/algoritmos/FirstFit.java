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

    public int buscarEspaco(int tamanhoSlots, int inicioBusca, int fimBusca) { //função principal
        int[] memoria = heap.getMemoria(); //Pega referencia da Heap

        int inicioLivre = -1; //marca onde começa o espaço livre
        int contadorLivre = 0; // conta quantos slots livres foram encontrados

        for (int i = inicioBusca; i < fimBusca; i++) { // percorre a heap
            if (memoria[i] == 0) { //se o slot está livre
                if (inicioLivre == -1) {
                    inicioLivre = i;
                }

                contadorLivre++;

                if (contadorLivre >= tamanhoSlots) { // se acha um espaço de tamanho livre necessário retorna a posição
                    return inicioLivre;
                }
            } else {
                inicioLivre = -1;
                contadorLivre = 0;
            }
        }

        return -1; // retorna falha
    }
}