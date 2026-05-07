package simulador.heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GerenciadorHeap {
    private Heap heap;
    private ArrayList<BlocoAlocado> tabelaAlocacoes;
    private int proximoId;
    private int totalChamadasLiberacao;
    private int totalBlocosLiberados;

    public GerenciadorHeap(Heap heap) {
        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = 1;
        this.totalChamadasLiberacao = 0;
        this.totalBlocosLiberados = 0;
    }

    public int gerarNovoId() {
        int idGerado = proximoId;
        proximoId++;
        return idGerado;
    }

    public void registrarBlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser maior que zero, pois 0 representa espaço livre");
        }

        if (tamanhoSlots <= 0) {
            throw new IllegalArgumentException("O tamanho em slots deve ser maior que zero");
        }

        if (!intervaloEstaDentroDaHeap(inicio, tamanhoSlots)) {
            throw new IllegalArgumentException("O bloco informado está fora dos limites da heap");
        }

        if (buscarBlocoPorId(id) != null) {
            throw new IllegalArgumentException("Já existe um bloco com o ID " + id);
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
                // mesmo após liberação segue sem espaço contíguo (frag externa)
                compactar();

                // última tentativa após compactação
                inicio = buscarEspacoFirstFit(tamanhoSlots);

                if (inicio == -1) {
                    // mesmo depois de liberar e compactar não tem espaço, a heap não tem capacidade pra ess requisição
                    System.out.printf("\nREQUISIÇÃO REJEITADA: sem espaço após liberação e compactação\n");
                    return -1;
                }
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

    if (tabelaAlocacoes.isEmpty()) {
        System.out.println("Tabela vazia, nada para liberar");
        return;
    }

    int metaSlots = (int) Math.ceil(heap.getTotalSlots() * 0.30);
    int metaBytes = metaSlots * 4;
    int slotsLiberados = 0;

    System.out.printf("\nINICIANDO LIBERAÇÃO: %d slots (%d  bytes)\n", metaSlots, metaBytes);

    // copia a lista de blocos e embaralha para garantir ordem aleatória
    // usamos uma cópia para nao modificar a lista original durante o loop
    ArrayList<BlocoAlocado> candidatos = new ArrayList<>(tabelaAlocacoes);
    Collections.shuffle(candidatos, new Random());

    for (int i = 0; i < candidatos.size() && slotsLiberados < metaSlots; i++) {
        BlocoAlocado bloco = candidatos.get(i);

        // zera os slots na heap e remove da tabela
        heap.liberar(bloco.getInicio(), bloco.getTamanhoSlots());
        removerRegistroPorId(bloco.getId());

        slotsLiberados += bloco.getTamanhoSlots();
        totalBlocosLiberados++;

        System.out.printf("\nRemovido ID = %d | slots = %d | liberado até agora = %d/%d\n", bloco.getId(), bloco.getTamanhoSlots(), slotsLiberados, metaSlots);
    }

    // avisa se não conseguiu atingir a meta (poucos blocos na tabela)
    if (slotsLiberados < metaSlots) {
        System.out.printf("\nMeta não atingida\nLiberado = %d | Meta = %d\nBlocos Insuficientes\n", slotsLiberados, metaSlots);
    } else {
        System.out.printf("\nLiberação Concluída.\nTotal liberado = %d slots (%d bytes)\n", slotsLiberados, slotsLiberados*4);
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
            System.out.printf("\nNão tem blocos livres\n");
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
        System.out.printf("\n\n=== Resumo do Controle da Heap ===");
        System.out.printf("\nTotal de slots da heap: %d", heap.getTotalSlots());
        System.out.printf("\nBlocos alocados: %d", getQuantidadeBlocosAlocados());
        System.out.printf("\nSlots ocupados pela tabela: %d", contarSlotsOcupadosPelaTabela());
        System.out.printf("\nSlots livres pela tabela: %d", contarSlotsLivresPelaTabela());
        System.out.printf("\nSlots ocupados olhando a heap: %d", contarSlotsOcupadosPelaHeap());
        System.out.printf("\nSlots livres olhando a heap: %d\n", contarSlotsLivresPelaHeap());
    }

    // compacta a heap movendo todos os blocos ocupados para o início,
    // eliminando os buracos causados pela fragmentação externa.
    // deve ser chamada após a liberação aleatória, quando ainda não
    // há espaço contíguo suficiente para a nova alocação.
    public void compactar() {
        int[] memoria = heap.getMemoria();
        int posicaoEscrita = 0; // próxima posição livre no início da heap
 
        System.out.printf("\nINICIANDO COMPACTAÇÃO\n");

        // percorre toda a heap slot por slot
        for (int i = 0; i < memoria.length; i++) {
            // slot ocupado — precisa ser movido para mais perto do início
            if (memoria[i] != 0) {
                int idBloco = memoria[i];

                // só processa o primeiro slot de cada bloco
                // os demais slots do mesmo bloco serão copiados no loop interno
                BlocoAlocado bloco = buscarBlocoPorId(idBloco);

                // segurança: ignora se o bloco não está na tabela
                if (bloco == null) continue;

                // salva o início original ANTES de qualquer modificação
                int inicioOriginal = bloco.getInicio();

                if (inicioOriginal != posicaoEscrita) {
                    // copia os slots do bloco para a nova posição
                    for (int j = 0; j < bloco.getTamanhoSlots(); j++) {
                        memoria[posicaoEscrita + j] = idBloco;
                    }

                    // apaga os slots antigos do bloco
                    for (int j = posicaoEscrita + bloco.getTamanhoSlots(); j < inicioOriginal + bloco.getTamanhoSlots(); j++) {
                        if (j >= 0 && j < memoria.length) {
                            memoria[j] = 0;
                        }
                    }

                    System.out.printf("Bloco ID = %d movido: %d -> %d\n", idBloco, inicioOriginal, posicaoEscrita);

                    // atualiza o início do bloco na tabela
                    bloco.setInicio(posicaoEscrita);
                }

                // avança a posição de escrita pelo tamanho do bloco
                posicaoEscrita += bloco.getTamanhoSlots();

                // usa inicioOriginal para pular corretamente que já foi atualizado para a nova posição
                i = inicioOriginal + bloco.getTamanhoSlots() - 1;
            }
        }

        // zera tudo depois do ultimo bloco
        for (int x = posicaoEscrita; x < memoria.length; x++) {
            memoria[x] = 0;
        }

        System.out.printf("COMPACTAÇÃO CONCLUÍDA. Espaço livre a partir do slot %d\n", posicaoEscrita);
    }
}
