package simulador.heap;

import simulador.algoritmos.Compactacao;
import simulador.algoritmos.FirstFit;
import simulador.algoritmos.Libera;
import simulador.sincronizacao.SemaforoHeap;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
// CLASSE RESPONSÁVEL POR TODAS OPERAÇÕES SOBRE A HEAP//

public class GerenciadorHeap {
    private final Heap heap; //vetor da heap
    private final ArrayList<BlocoAlocado> tabelaAlocacoes; 
    private final AtomicInteger proximoId; //incrementa o id em 1
    private final AtomicInteger totalChamadasLiberacao; //armazenar estatísticas especifiadas no projeto
    private final AtomicInteger totalBlocosLiberados; //armazenar estatísticas especifiadas no projeto
    private final AtomicInteger totalChamadasCompactacao; //armazenar estatísticas especifiadas no projeto 
    private final SemaforoHeap[] semaforosSegmentos; //Semáforo para proteger segmentos da heap
    private final SemaforoHeap semaforoTabela; //mutex para a tabela de alocações

    //usado para a versão sequencial
    public GerenciadorHeap(Heap heap) {
        this(heap, 1); 
    }

    //usado para a versão paralela
    public GerenciadorHeap(Heap heap, int totalSegmentos) {
        if (totalSegmentos <= 0) {  //minimo 1 segmento
            totalSegmentos = 1;
        }

        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = new AtomicInteger(1); // para incrementar o id
        this.totalChamadasLiberacao = new AtomicInteger(0);
        this.totalBlocosLiberados = new AtomicInteger(0);
        this.totalChamadasCompactacao = new AtomicInteger(0);
        this.semaforosSegmentos = new SemaforoHeap[totalSegmentos];
        this.semaforoTabela = new SemaforoHeap();

        for (int i = 0; i < totalSegmentos; i++) { //a partir da quantidade de segmentos gera um semáforo de contagem
            semaforosSegmentos[i] = new SemaforoHeap();
        }
    }

    public int alocarFirstFit(int tamanhoBytes) {
        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);

        if (tamanhoBytes <= 0 || tamanhoSlots > heap.getTotalSlots()) { //requisições maiores que a heap ou inválidas são descartadas
            return -1;
        }
        // calcula o segmento inicial da thread com base no nome
        int inicioSegmento = Math.abs(Thread.currentThread().getName().hashCode()) % semaforosSegmentos.length; 
        
        //Procura algum segmento da heap para alocar
        for (int tentativa = 0; tentativa < semaforosSegmentos.length; tentativa++) {
            int segmento = (inicioSegmento + tentativa) % semaforosSegmentos.length;
 
            //Se o segmento estiver bloqueado, tenta o próximo
            if (semaforosSegmentos[segmento].tentarAdquirir()) {
                int id = alocarNoSegmento(tamanhoBytes, tamanhoSlots, segmento);

                semaforosSegmentos[segmento].liberar(); //libera o segmento apos a alocação

                if (id != -1) {
                    return id; 
                }
            }
        }

        return alocarComBloqueioGlobal(tamanhoBytes, tamanhoSlots); // se não consegiu alocar em um segmento bloqueia a heap
    }

    private int alocarNoSegmento(int tamanhoBytes, int tamanhoSlots, int segmento) {
      
        int inicioSegmento = calcularInicioSegmento(segmento);
        int fimSegmento = calcularFimSegmento(segmento);
       
        FirstFit firstFit = new FirstFit(heap);
        int inicio = firstFit.buscarEspaco(tamanhoSlots, inicioSegmento, fimSegmento); //tenta alocar o bloco no intervalo do segmento

        if (inicio == -1) { // se não encotrou espaço retorna falha
            return -1;
        }

        int id = gerarNovoId(); // gera um id para o bloo

        heap.escrever(inicio, tamanhoSlots, id); // escreve na heap
        registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots); //escreve na tabelça de alocação

        return id;
    }

    private int alocarComBloqueioGlobal(int tamanhoBytes, int tamanhoSlots) { 
        adquirirTodosSegmentos(); //bloqueia toda heap

        FirstFit firstFit = new FirstFit(heap);
        int inicio = firstFit.buscarEspaco(tamanhoSlots);

        if (inicio == -1) {
            int slotsLivres = contarSlotsLivresPelaHeap();

            if (slotsLivres >= tamanhoSlots) { // se tem espaço livre, há fragmentação então chama compactação
                compactarInterno();
            } else { // se não tem espaço livre libera 30% da heap e compacta
                liberarAleatorioInterno();
                compactarInterno();
            }

            inicio = firstFit.buscarEspaco(tamanhoSlots); // tenta alocar novamente
        }

        int id = -1;

        if (inicio != -1) { // se achou espaço, realiza a alocação (heap e tabela)
            id = gerarNovoId();

            heap.escrever(inicio, tamanhoSlots, id);
            registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);
        }

        liberarTodosSegmentos(); //lbera a heap

        return id;
    }

    private void adquirirTodosSegmentos() { //impedir que outras threads entrem na heap
        for (SemaforoHeap semaforo : semaforosSegmentos) {
            semaforo.adquirir();
        }
    }

    private void liberarTodosSegmentos() { //libera a heap inteira
        for (SemaforoHeap semaforo : semaforosSegmentos) { //libera todos os semáforos
            semaforo.liberar();
        }
    }

    private int calcularInicioSegmento(int segmento) { 
        return (heap.getTotalSlots() * segmento) / semaforosSegmentos.length; // cada segmento teu seu proprio semáforo
    }

    private int calcularFimSegmento(int segmento) {
        return (heap.getTotalSlots() * (segmento + 1)) / semaforosSegmentos.length; 
    }

    //gera um ID único
    public int gerarNovoId() {
        return proximoId.getAndIncrement(); //retorna o valor atual e incrementa para o próximo 
    }

    //adiciona um bloco na tabela de alocações
    public void registrarBlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        semaforoTabela.adquirir(); //bloqueia a tabela

        tabelaAlocacoes.add(new BlocoAlocado(id, inicio, tamanhoSolicitadoBytes, tamanhoSlots)); //adiciona o bloco na tabela

        semaforoTabela.liberar(); //libera a tabela
    }

    //procura um bloco na tabela por ID
    public BlocoAlocado buscarBlocoPorId(int id) {
        semaforoTabela.adquirir(); //bloqueia a tabela

        BlocoAlocado resultado = null; //inicia dizendo que não encontrou nada

        for (BlocoAlocado bloco : tabelaAlocacoes) { //percorre a tabela
            if (bloco.getId() == id) { //se encontrou o ID, guarda em resultado e para o loop
                resultado = bloco;
                break;
            }
        }

        semaforoTabela.liberar(); //libera a tabela

        return resultado; //retorna o bloco que foi encontrado
    }

    //remove um bloco por ID
    public boolean removerRegistroPorId(int id) {
        semaforoTabela.adquirir(); //bloqueia a tabela

        boolean removeu = false; //inicia dizendo que não removeu

        for (int i = 0; i < tabelaAlocacoes.size(); i++) { //percorre a tabela
            if (tabelaAlocacoes.get(i).getId() == id) { //verifica se o bloco tem o ID procurado
                tabelaAlocacoes.remove(i); //remove o bloco e para o loop
                removeu = true; 
                break;
            }
        }

        semaforoTabela.liberar(); //libera a tabela

        return removeu; //retorna o resultado
    }

    //atualiza a posição inicial do bloco. É usado na compactação
    public void atualizarInicioBloco(int id, int novoInicio) {
        semaforoTabela.adquirir(); //bloqueia a tabela

        for (BlocoAlocado bloco : tabelaAlocacoes) {
            if (bloco.getId() == id) { //se encontrou o bloco, atualiza o início dele
                bloco.setInicio(novoInicio);
                break;
            }
        }

        semaforoTabela.liberar(); //libera a tabela
    }

    //libera um bloco específico por ID
    public boolean liberarPorIdInterno(int id) { 
        BlocoAlocado bloco = buscarBlocoPorId(id); //procura o bloco na tabela

        if (bloco == null) { //se não encontrou, não tem nada pra liberar
            return false;
        }

        heap.liberar(bloco.getInicio(), bloco.getTamanhoSlots()); //zera as posições que esse bloco ocupa na heap
        removerRegistroPorId(id); //remove os blocos da tabela
        incrementarBlocosLiberados(); //incrementa a estatística de blocos liberados

        return true; //liberou com sucesso
    }

    //libera blocos aleatórios
    public void liberarAleatorio() {
        adquirirTodosSegmentos(); //bloqueia a heap
        liberarAleatorioInterno(); //libera os blocos aleatoriamente
        liberarTodosSegmentos(); // libera a heap
    }

    public void liberarAleatorioInterno() {
        new Libera(heap, this).liberar(); //chama o método que libera os 30%
    }

    //compactação da heap
    public void compactar() {
        adquirirTodosSegmentos(); //bloqueia a heap
        compactarInterno(); //faz a compactação
        liberarTodosSegmentos(); //libera a heap
    }

    //executa a compactação
    public void compactarInterno() {
        new Compactacao(heap, this).compactar();
    }

    //verifica se o intervalo está dentro da heap
    public boolean intervaloEstaDentroDaHeap(int inicio, int tamanhoSlots) {
        if (inicio < 0 || tamanhoSlots <= 0) { //se for um valor negativo é inválido
            return false;
        }

        return (inicio + tamanhoSlots - 1) < heap.getTotalSlots(); //verifica se a última posição do bloco está dentro da heap
    }

    //conta quantos slots estão ocupados de acordo com a tabela
    public int contarSlotsOcupadosPelaTabela() {
        int total = 0;

        semaforoTabela.adquirir(); //bloqueia a tabela

        for (BlocoAlocado bloco : tabelaAlocacoes) {
            total += bloco.getTamanhoSlots(); //soma o tamanho em slots de todos os blocos
        }

        semaforoTabela.liberar(); //libera a tabela

        return total; 
    }


    public int contarSlotsLivresPelaTabela() {
        return heap.getTotalSlots() - contarSlotsOcupadosPelaTabela();
    }

    //conta os slots livres da heap
    public int contarSlotsLivresPelaHeap() {
        int[] memoria = heap.getMemoria();
        int livres = 0;

        for (int slot : memoria) {
            if (slot == 0) { //se o slot estiver com 0 significa qque ele está livre
                livres++;
            }
        }

        return livres;
    }

    public int contarSlotsOcupadosPelaHeap() {
        return heap.getTotalSlots() - contarSlotsLivresPelaHeap();
    }

    //retorna uma cópia da tabela de alocações
    public ArrayList<BlocoAlocado> getTabelaAlocacoes() {
        semaforoTabela.adquirir();

        ArrayList<BlocoAlocado> copia = new ArrayList<>(tabelaAlocacoes); //cria a cópia da tabela

        semaforoTabela.liberar();

        return copia;
    }

    //retorna quantos blocos existem na tabela
    public int getQuantidadeBlocosAlocados() {
        semaforoTabela.adquirir();

        int quantidade = tabelaAlocacoes.size(); //pega a quantidade de blocos

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