package simulador.heap;

import simulador.algoritmos.Compactacao;
import simulador.algoritmos.FirstFit;
import simulador.algoritmos.Libera;
import simulador.sincronizacao.SemaforoHeap;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GerenciadorHeap {
    private final Heap heap;
    private final ArrayList<BlocoAlocado> tabelaAlocacoes;
    private final AtomicInteger proximoId;
    private final AtomicInteger totalChamadasLiberacao;
    private final AtomicInteger totalBlocosLiberados;
    private final AtomicInteger totalChamadasCompactacao;
    private final SemaforoHeap[] semaforosSegmentos;
    private final SemaforoHeap semaforoTabela;

    public GerenciadorHeap(Heap heap) {
        this(heap, 1);
    }

    public GerenciadorHeap(Heap heap, int totalSegmentos) {
        if (totalSegmentos <= 0) {
            totalSegmentos = 1;
        }

        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = new AtomicInteger(1);
        this.totalChamadasLiberacao = new AtomicInteger(0);
        this.totalBlocosLiberados = new AtomicInteger(0);
        this.totalChamadasCompactacao = new AtomicInteger(0);
        this.semaforosSegmentos = new SemaforoHeap[totalSegmentos];
        this.semaforoTabela = new SemaforoHeap();

        for (int i = 0; i < totalSegmentos; i++) {
            semaforosSegmentos[i] = new SemaforoHeap();
        }
    }

    public int alocarFirstFit(int tamanhoBytes) {
        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);

        if (tamanhoBytes <= 0 || tamanhoSlots > heap.getTotalSlots()) {
            return -1;
        }

        int inicioSegmento = Math.abs(Thread.currentThread().getName().hashCode()) % semaforosSegmentos.length;

        for (int tentativa = 0; tentativa < semaforosSegmentos.length; tentativa++) {
            int segmento = (inicioSegmento + tentativa) % semaforosSegmentos.length;

            if (semaforosSegmentos[segmento].tentarAdquirir()) {
                int id = alocarNoSegmento(tamanhoBytes, tamanhoSlots, segmento);

                semaforosSegmentos[segmento].liberar();

                if (id != -1) {
                    return id;
                }
            }
        }

        return alocarComBloqueioGlobal(tamanhoBytes, tamanhoSlots);
    }

    private int alocarNoSegmento(int tamanhoBytes, int tamanhoSlots, int segmento) {
        int inicioSegmento = calcularInicioSegmento(segmento);
        int fimSegmento = calcularFimSegmento(segmento);

        FirstFit firstFit = new FirstFit(heap);
        int inicio = firstFit.buscarEspaco(tamanhoSlots, inicioSegmento, fimSegmento);

        if (inicio == -1) {
            return -1;
        }

        int id = gerarNovoId();

        heap.escrever(inicio, tamanhoSlots, id);
        registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);

        return id;
    }

    private int alocarComBloqueioGlobal(int tamanhoBytes, int tamanhoSlots) {
        adquirirTodosSegmentos();

        FirstFit firstFit = new FirstFit(heap);
        int inicio = firstFit.buscarEspaco(tamanhoSlots);

        if (inicio == -1) {
            int slotsLivres = contarSlotsLivresPelaHeap();

            if (slotsLivres >= tamanhoSlots) {
                compactarInterno();
            } else {
                liberarAleatorioInterno();
                compactarInterno();
            }

            inicio = firstFit.buscarEspaco(tamanhoSlots);
        }

        int id = -1;

        if (inicio != -1) {
            id = gerarNovoId();

            heap.escrever(inicio, tamanhoSlots, id);
            registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);
        }

        liberarTodosSegmentos();

        return id;
    }

    private void adquirirTodosSegmentos() {
        for (SemaforoHeap semaforo : semaforosSegmentos) {
            semaforo.adquirir();
        }
    }

    private void liberarTodosSegmentos() {
        for (SemaforoHeap semaforo : semaforosSegmentos) {
            semaforo.liberar();
        }
    }

    private int calcularInicioSegmento(int segmento) {
        return (heap.getTotalSlots() * segmento) / semaforosSegmentos.length;
    }

    private int calcularFimSegmento(int segmento) {
        return (heap.getTotalSlots() * (segmento + 1)) / semaforosSegmentos.length;
    }

    public int gerarNovoId() {
        return proximoId.getAndIncrement();
    }

    public void registrarBlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        semaforoTabela.adquirir();

        tabelaAlocacoes.add(new BlocoAlocado(id, inicio, tamanhoSolicitadoBytes, tamanhoSlots));

        semaforoTabela.liberar();
    }

    public BlocoAlocado buscarBlocoPorId(int id) {
        semaforoTabela.adquirir();

        BlocoAlocado resultado = null;

        for (BlocoAlocado bloco : tabelaAlocacoes) {
            if (bloco.getId() == id) {
                resultado = bloco;
                break;
            }
        }

        semaforoTabela.liberar();

        return resultado;
    }

    public boolean removerRegistroPorId(int id) {
        semaforoTabela.adquirir();

        boolean removeu = false;

        for (int i = 0; i < tabelaAlocacoes.size(); i++) {
            if (tabelaAlocacoes.get(i).getId() == id) {
                tabelaAlocacoes.remove(i);
                removeu = true;
                break;
            }
        }

        semaforoTabela.liberar();

        return removeu;
    }

    public void atualizarInicioBloco(int id, int novoInicio) {
        semaforoTabela.adquirir();

        for (BlocoAlocado bloco : tabelaAlocacoes) {
            if (bloco.getId() == id) {
                bloco.setInicio(novoInicio);
                break;
            }
        }

        semaforoTabela.liberar();
    }

    public boolean liberarPorIdInterno(int id) {
        BlocoAlocado bloco = buscarBlocoPorId(id);

        if (bloco == null) {
            return false;
        }

        heap.liberar(bloco.getInicio(), bloco.getTamanhoSlots());
        removerRegistroPorId(id);
        incrementarBlocosLiberados();

        return true;
    }

    public void liberarAleatorio() {
        adquirirTodosSegmentos();
        liberarAleatorioInterno();
        liberarTodosSegmentos();
    }

    public void liberarAleatorioInterno() {
        new Libera(heap, this).liberar();
    }

    public void compactar() {
        adquirirTodosSegmentos();
        compactarInterno();
        liberarTodosSegmentos();
    }

    public void compactarInterno() {
        new Compactacao(heap, this).compactar();
    }

    public boolean intervaloEstaDentroDaHeap(int inicio, int tamanhoSlots) {
        if (inicio < 0 || tamanhoSlots <= 0) {
            return false;
        }

        return (inicio + tamanhoSlots - 1) < heap.getTotalSlots();
    }

    public int contarSlotsOcupadosPelaTabela() {
        int total = 0;

        semaforoTabela.adquirir();

        for (BlocoAlocado bloco : tabelaAlocacoes) {
            total += bloco.getTamanhoSlots();
        }

        semaforoTabela.liberar();

        return total;
    }

    public int contarSlotsLivresPelaTabela() {
        return heap.getTotalSlots() - contarSlotsOcupadosPelaTabela();
    }

    public int contarSlotsLivresPelaHeap() {
        int[] memoria = heap.getMemoria();
        int livres = 0;

        for (int slot : memoria) {
            if (slot == 0) {
                livres++;
            }
        }

        return livres;
    }

    public int contarSlotsOcupadosPelaHeap() {
        return heap.getTotalSlots() - contarSlotsLivresPelaHeap();
    }

    public ArrayList<BlocoAlocado> getTabelaAlocacoes() {
        semaforoTabela.adquirir();

        ArrayList<BlocoAlocado> copia = new ArrayList<>(tabelaAlocacoes);

        semaforoTabela.liberar();

        return copia;
    }

    public int getQuantidadeBlocosAlocados() {
        semaforoTabela.adquirir();

        int quantidade = tabelaAlocacoes.size();

        semaforoTabela.liberar();

        return quantidade;
    }

    public void incrementarChamadasLiberacao() {
        totalChamadasLiberacao.incrementAndGet();
    }

    public void incrementarBlocosLiberados() {
        totalBlocosLiberados.incrementAndGet();
    }

    public void incrementaChamadasCompactacao() {
        totalChamadasCompactacao.incrementAndGet();
    }

    public int getTotalBlocosLiberados() {
        return totalBlocosLiberados.get();
    }

    public int getTotalChamadasLiberacao() {
        return totalChamadasLiberacao.get();
    }

    public int getTotalChamadasCompactacao() {
        return totalChamadasCompactacao.get();
    }
}