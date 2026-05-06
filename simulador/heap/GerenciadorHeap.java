package simulador.heap;

import java.util.ArrayList;

public class GerenciadorHeap {
    private Heap heap;
    private ArrayList<BlocoAlocado> tabelaAlocacoes;
    private int proximoId;

    public GerenciadorHeap(Heap heap) {
        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = 1;
    }

    public int gerarNovoId() {
        int idGerado = proximoId;
        proximoId++;
        return idGerado;
    }

    public void registrarBlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser maior que zero, pois 0 representa espaço livre.");
        }

        if (tamanhoSlots <= 0) {
            throw new IllegalArgumentException("O tamanho em slots deve ser maior que zero.");
        }

        if (!intervaloEstaDentroDaHeap(inicio, tamanhoSlots)) {
            throw new IllegalArgumentException("O bloco informado está fora dos limites da heap.");
        }

        if (buscarBlocoPorId(id) != null) {
            throw new IllegalArgumentException("Já existe um bloco com o ID " + id + ".");
        }

        BlocoAlocado bloco = new BlocoAlocado(id, inicio, tamanhoSolicitadoBytes, tamanhoSlots);
        tabelaAlocacoes.add(bloco);
    }

    public BlocoAlocado buscarBlocoPorId(int id) {
        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            BlocoAlocado bloco = tabelaAlocacoes.get(i);

            if (bloco.getId() == id) {
                return bloco;
            }
        }

        return null;
    }

    public boolean removerRegistroPorId(int id) {
        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            BlocoAlocado bloco = tabelaAlocacoes.get(i);

            if (bloco.getId() == id) {
                tabelaAlocacoes.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean intervaloEstaDentroDaHeap(int inicio, int tamanhoSlots) {
        if (inicio < 0) {
            return false;
        }

        if (tamanhoSlots <= 0) {
            return false;
        }

        int fim = inicio + tamanhoSlots - 1;

        return fim < heap.getTotalSlots();
    }

    public boolean intervaloEstaLivre(int inicio, int tamanhoSlots) {
        if (!intervaloEstaDentroDaHeap(inicio, tamanhoSlots)) {
            return false;
        }

        int[] memoria = heap.getMemoria();

        for (int i = inicio; i < inicio + tamanhoSlots; i++) {
            if (memoria[i] != 0) {
                return false;
            }
        }

        return true;
    }

    public int contarSlotsOcupadosPelaTabela() {
        int total = 0;

        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            BlocoAlocado bloco = tabelaAlocacoes.get(i);
            total += bloco.getTamanhoSlots();
        }

        return total;
    }

    public int contarSlotsLivresPelaTabela() {
        return heap.getTotalSlots() - contarSlotsOcupadosPelaTabela();
    }

    public int contarSlotsLivresPelaHeap() {
        int[] memoria = heap.getMemoria();
        int livres = 0;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                livres++;
            }
        }

        return livres;
    }

    public int contarSlotsOcupadosPelaHeap() {
        return heap.getTotalSlots() - contarSlotsLivresPelaHeap();
    }

    public void atualizarInicioBloco(int id, int novoInicio) {
        BlocoAlocado bloco = buscarBlocoPorId(id);

        if (bloco != null) {
            bloco.setInicio(novoInicio);
        }
    }

    public ArrayList<BlocoAlocado> getTabelaAlocacoes() {
        return tabelaAlocacoes;
    }

    public int getQuantidadeBlocosAlocados() {
        return tabelaAlocacoes.size();
    }

    public void imprimirTabelaAlocacoes() {
        System.out.printf("\n=== Tabela de Blocos Ocupados ===\n");

        if (tabelaAlocacoes.isEmpty()) {
            System.out.printf("Nenhum bloco alocado\n");
            return;
        }

        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            System.out.println(tabelaAlocacoes.get(i));
        }
    }

    public void imprimirBlocosLivresPelaHeap() {
        System.out.printf("\n=== Blocos Livres na Heap ===\n");

        int[] memoria = heap.getMemoria();

        int inicioLivre = -1;
        int tamanhoLivre = 0;
        boolean encontrouLivre = false;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                if (inicioLivre == -1) {
                    inicioLivre = i;
                    tamanhoLivre = 1;
                } else {
                    tamanhoLivre++;
                }
            } else {
                if (inicioLivre != -1) {
                    imprimirBlocoLivre(inicioLivre, tamanhoLivre);
                    encontrouLivre = true;

                    inicioLivre = -1;
                    tamanhoLivre = 0;
                }
            }
        }

        if (inicioLivre != -1) {
            imprimirBlocoLivre(inicioLivre, tamanhoLivre);
            encontrouLivre = true;
        }

        if (!encontrouLivre) {
            System.out.printf("\nNão há blocos livres\n");
        }
    }

    private void imprimirBlocoLivre(int inicio, int tamanhoSlots) {
        int fim = inicio + tamanhoSlots - 1;
        int tamanhoBytes = tamanhoSlots * 4;

        System.out.println("Livre | Início: " + inicio
                + " | Fim: " + fim
                + " | Slots: " + tamanhoSlots
                + " | Bytes: " + tamanhoBytes);
    }

    public void imprimirResumoControle() {
        System.out.println("\n=== Resumo do Controle da Heap ===");
        System.out.println("Total de slots da heap: " + heap.getTotalSlots());
        System.out.println("Blocos alocados: " + getQuantidadeBlocosAlocados());
        System.out.println("Slots ocupados pela tabela: " + contarSlotsOcupadosPelaTabela());
        System.out.println("Slots livres pela tabela: " + contarSlotsLivresPelaTabela());
        System.out.println("Slots ocupados olhando a heap: " + contarSlotsOcupadosPelaHeap());
        System.out.println("Slots livres olhando a heap: " + contarSlotsLivresPelaHeap());
    }
}
