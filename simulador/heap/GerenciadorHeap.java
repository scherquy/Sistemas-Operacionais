package simulador.heap;

import simulador.algoritmos.Compactacao;
import simulador.algoritmos.FirstFit;
import simulador.algoritmos.Libera;

import java.util.ArrayList;

public class GerenciadorHeap {
    private Heap heap;
    private ArrayList<BlocoAlocado> tabelaAlocacoes;
    private int proximoId;
    private int totalChamadasLiberacao;
    private int totalBlocosLiberados;
    private int totalChamadasCompactacao;

    public GerenciadorHeap(Heap heap) {
        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = 1;
        this.totalChamadasLiberacao = 0;
        this.totalBlocosLiberados = 0;
        this.totalChamadasCompactacao = 0;
    }

    public int gerarNovoId() {
        return proximoId++;
    }

    public void registrarBlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        if (id <= 0) throw new IllegalArgumentException("O ID deve ser maior que zero");
        if (tamanhoSlots <= 0) throw new IllegalArgumentException("O tamanho em slots deve ser maior que zero");
        if (!intervaloEstaDentroDaHeap(inicio, tamanhoSlots)) throw new IllegalArgumentException("O bloco está fora dos limites da heap");
        if (buscarBlocoPorId(id) != null) throw new IllegalArgumentException("Já existe um bloco com o ID " + id);

        tabelaAlocacoes.add(new BlocoAlocado(id, inicio, tamanhoSolicitadoBytes, tamanhoSlots));
    }

    public BlocoAlocado buscarBlocoPorId(int id) {
        for (BlocoAlocado bloco : tabelaAlocacoes) {
            if (bloco.getId() == id) return bloco;
        }
        return null;
    }

    public boolean removerRegistroPorId(int id) {
        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            if (tabelaAlocacoes.get(i).getId() == id) {
                tabelaAlocacoes.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean intervaloEstaDentroDaHeap(int inicio, int tamanhoSlots) {
        if (inicio < 0 || tamanhoSlots <= 0) return false;
        return (inicio + tamanhoSlots - 1) < heap.getTotalSlots();
    }

    public boolean intervaloEstaLivre(int inicio, int tamanhoSlots) {
        if (!intervaloEstaDentroDaHeap(inicio, tamanhoSlots)) return false;
        int[] memoria = heap.getMemoria();
        for (int i = inicio; i < inicio + tamanhoSlots; i++) {
            if (memoria[i] != 0) return false;
        }
        return true;
    }

    public boolean liberarPorId(int id) {
        BlocoAlocado bloco = buscarBlocoPorId(id);

        if (bloco == null) {
            System.out.printf("\nID %d não encontrado. Nada foi liberado\n", id);
            return false;
        }

        heap.liberar(bloco.getInicio(), bloco.getTamanhoSlots());
        removerRegistroPorId(id);
        incrementarBlocosLiberados();

        return true;
    }

    public int alocarFirstFit(int tamanhoBytes) {
        return new FirstFit(heap, this).alocar(tamanhoBytes);
    }

    public void liberarAleatorio() {
        new Libera(heap, this).liberar();
    }

    public void compactar() {
        new Compactacao(heap, this).compactar();
    }

    public void incrementarChamadasLiberacao() {
        totalChamadasLiberacao++;
    }

    public void incrementarBlocosLiberados() {
        totalBlocosLiberados++;
    }

    public void incrementaChamadasCompactacao(){
        totalChamadasCompactacao++;
    }

    public int contarSlotsOcupadosPelaTabela() {
        int total = 0;
        for (BlocoAlocado bloco : tabelaAlocacoes) total += bloco.getTamanhoSlots();
        return total;
    }

    public int contarSlotsLivresPelaTabela() {
        return heap.getTotalSlots() - contarSlotsOcupadosPelaTabela();
    }

    public int contarSlotsLivresPelaHeap() {
        int[] memoria = heap.getMemoria();
        int livres = 0;
        for (int slot : memoria) if (slot == 0) livres++;
        return livres;
    }

    public int contarSlotsOcupadosPelaHeap() {
        return heap.getTotalSlots() - contarSlotsLivresPelaHeap();
    }

    public ArrayList<BlocoAlocado> getTabelaAlocacoes() {
        return tabelaAlocacoes;
    }

    public int getQuantidadeBlocosAlocados() {
        return tabelaAlocacoes.size();
    }

    public int getTotalBlocosLiberados() {
        return totalBlocosLiberados;
    }

    public int getTotalChamadasLiberacao() {
        return totalChamadasLiberacao;
    }

    public int getTotalChamadasCompactacao(){
        return totalChamadasCompactacao;
    }

    public void imprimirTabelaAlocacoes() {
        System.out.printf("\n=== Tabela de Blocos Ocupados ===\n");
        if (tabelaAlocacoes.isEmpty()) {
            System.out.printf("Nenhum bloco alocado\n");
            return;
        }
        for (BlocoAlocado bloco : tabelaAlocacoes) System.out.println(bloco);
        System.err.println();
    }

    public void imprimirBlocosLivresPelaHeap() {
        System.out.printf("\n=== Blocos Livres na Heap ===\n");
        int[] memoria = heap.getMemoria();
        int inicioLivre = -1;
        int tamanhoLivre = 0;
        boolean encontrouLivre = false;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                if (inicioLivre == -1) { inicioLivre = i; tamanhoLivre = 1; }
                else tamanhoLivre++;
            } else if (inicioLivre != -1) {
                imprimirBlocoLivre(inicioLivre, tamanhoLivre);
                encontrouLivre = true;
                inicioLivre = -1;
                tamanhoLivre = 0;
            }
        }

        if (inicioLivre != -1) { imprimirBlocoLivre(inicioLivre, tamanhoLivre); encontrouLivre = true; }
        if (!encontrouLivre) System.out.printf("\nNão tem blocos livres\n");
    }

    private void imprimirBlocoLivre(int inicio, int tamanhoSlots) {
        System.out.println("Livre | Início: " + inicio + " | Fim: " + (inicio + tamanhoSlots - 1)
                + " | Slots: " + tamanhoSlots + " | Bytes: " + (tamanhoSlots * 4));
    }

    public void imprimirResumoControle() {
        System.out.printf("\n\n=== Resumo do Controle da Heap ===");
        System.out.printf("\nTotal de slots da heap: %d", heap.getTotalSlots());
        System.out.printf("\nBlocos alocados: %d", getQuantidadeBlocosAlocados());
        System.out.printf("\nSlots ocupados pela tabela: %d", contarSlotsOcupadosPelaTabela());
        System.out.printf("\nSlots livres pela tabela: %d", contarSlotsLivresPelaTabela());
        System.out.printf("\nSlots ocupados olhando a heap: %d", contarSlotsOcupadosPelaHeap());
        System.out.printf("\nSlots livres olhando a heap: %d\n", contarSlotsLivresPelaHeap());
    }
}
