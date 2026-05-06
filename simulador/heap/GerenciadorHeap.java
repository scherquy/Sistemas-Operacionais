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


   // procura o primeiro espaço livre contíugo com tamanho suficiente para alocar o bloco(First Fit)
   //retorna o índice de início do espaço encontrado ou -1 se não houver espaço suficiente
 
    public int buscarEspacoFirstFit(int tamanhoSlots) {
        int[] memoria = heap.getMemoria();
        int inicioLivre = -1;
        int contadorLivre = 0;
 
        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {
                // marca o início do bloco livre se ainda não marcou
                if (inicioLivre == -1) {
                    inicioLivre = i;
                }
                contadorLivre++;
 
                // encontrou espaço suficiente: retorna imediatamente (First Fit)
                if (contadorLivre >= tamanhoSlots) {
                    return inicioLivre;
                }
            } else {
                // slot ocupado: reinicia a busca
                inicioLivre = -1;
                contadorLivre = 0;
            }
        }
 
        // nenhum espaço contíguo suficiente foi encontrado
        return -1;
    }

    // tenta alocar um bloco na heap usando First Fit
    // retorna o ID do bloco alocado, ou -1 se não houver espaço
    public int alocarFirstFit(int tamanhoBytes) {
        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);
        int inicio = buscarEspacoFirstFit(tamanhoSlots);
 
        if (inicio == -1) {
            // sem espaço: aciona a liberação aleatória de pelo menos 30% da heap
            liberarAleatorio();
 
            // tenta novamente após a liberação
            inicio = buscarEspacoFirstFit(tamanhoSlots);
 
            if (inicio == -1) {
                // mesmo após liberação não há espaço contíguo suficiente
                // isso pode ocorrer por fragmentação externa: compactação seria necessária aqui
                // por ora retorna -1 para o gerador tratar
                return -1;
            }
        }
 
        // espaço encontrado: gera ID, escreve na heap e registra na tabela
        int id = gerarNovoId();
        heap.escrever(inicio, tamanhoSlots, id);
        registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);
 
        return id;
    }

    // libera aleatoriamente blocos da heap até que pelo menos 30% do total de slots seja liberado
    
    public void liberarAleatorio() {
        totalChamadasLiberacao++;
 
        int metaLiberacao = (int) Math.ceil(heap.getTotalSlots() * 0.30);
        int slotsLiberados = 0;
 
        // copia a lista e embaralha para garantir ordem aleatória
        ArrayList<BlocoAlocado> candidatos = new ArrayList<>(tabelaAlocacoes);
        Collections.shuffle(candidatos, new Random());
 
        for (int i = 0; i < candidatos.size() && slotsLiberados < metaLiberacao; i++) {
            BlocoAlocado bloco = candidatos.get(i);
 
            // zera os slots na heap e remove da tabela
            heap.liberar(bloco.getInicio(), bloco.getTamanhoSlots());
            removerRegistroPorId(bloco.getId());
 
            slotsLiberados += bloco.getTamanhoSlots();
            totalBlocosLiberados++;
        }
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
