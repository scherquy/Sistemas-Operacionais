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

    // procura o primeiro espaco livre contiguo com tamanho suficiente
    // retorna o indice inicial do espaço encontrado
    // se nao encontrar retorna -1
    public int buscarEspaco(int tamanhoSlots) {
        int[] memoria = heap.getMemoria();

        int inicioLivre = -1;
        int contadorLivre = 0;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                if (inicioLivre == -1) {
                    inicioLivre = i;
                }

                contadorLivre++;

                if (contadorLivre >= tamanhoSlots) {
                    return inicioLivre;
                }
            }   else {
                    inicioLivre = -1;
                    contadorLivre = 0;
                }
        }

        return -1;
    }

    // tenta alocar um bloco na heap usando First Fit e retorna o ID do bloco alocado
    // se nao conseguir alocar retorna -1
    public int alocar(int tamanhoBytes) {
        if (tamanhoBytes <= 0) {
            throw new IllegalArgumentException("O tamanho em bytes deve ser maior que zero");
        }

        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);

        if (tamanhoSlots > heap.getTotalSlots()) {
            System.out.printf("\nREQUISIÇÃO REJEITADA: tamanho maior que a heap inteira\n");
            return -1;
        }

        int inicio = buscarEspaco(tamanhoSlots);

        if (inicio == -1) {
            int slotsLivres = gerenciador.contarSlotsLivresPelaHeap();

            if (slotsLivres >= tamanhoSlots) {
                // existe espaço livre suficiente no total mas ele ta espalhado pela heap entao o problema eh fragmentação externa
                gerenciador.compactar();
            } else {
                // nao existe espaco livre suficiente entao precisa remover blocos aleatorios
                gerenciador.liberarAleatorio();
                // depois da liberacao compacta para juntar o espaço livre
                gerenciador.compactar();
            }

            // tenta de novo depois da compactacao ou liberacao
            inicio = buscarEspaco(tamanhoSlots);

            if (inicio == -1) {
                System.out.printf("\nREQUISIÇÃO REJEITADA: sem espaço após liberação e compactação\n");
                return -1;
            }
        }

        int id = gerenciador.gerarNovoId();

        heap.escrever(inicio, tamanhoSlots, id);
        gerenciador.registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);

        return id;
    }
}
