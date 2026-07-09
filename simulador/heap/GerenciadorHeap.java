package simulador.heap;

import simulador.algoritmos.Compactacao;
import simulador.algoritmos.FirstFit;
import simulador.algoritmos.Libera;
import simulador.sincronizacao.SemaforoHeap;

import java.util.ArrayList;

public class GerenciadorHeap {
    private Heap heap;
    private ArrayList<BlocoAlocado> tabelaAlocacoes;
    private int proximoId;
    private int totalChamadasLiberacao;
    private int totalBlocosLiberados;
    private int totalChamadasCompactacao;
    private final SemaforoHeap semaforo;
  
    public GerenciadorHeap(Heap heap) {
        this.heap = heap;
        this.tabelaAlocacoes = new ArrayList<>();
        this.proximoId = 1;
        this.totalChamadasLiberacao = 0;
        this.totalBlocosLiberados = 0;
        this.totalChamadasCompactacao = 0;
        this.semaforo = new SemaforoHeap();
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

    // Este método pega o semáforo antes de modificar a heap.
    // é usado quando a chamada vem de fora da seção crítica.
    
    public boolean liberarPorId(int id) {
        boolean entrou = semaforo.adquirir();

        if (!entrou) {
            System.err.printf("\nERRO. Não foi possível acessar a heap para liberar o ID %d.\n", id);
            return false;
        }

        try {
            return liberarPorIdInterno(id);
        }   finally {
                semaforo.liberar();
            }
    }

    /*
    Método interno para liberar um bloco pelo ID.

    Este método não pega o semáforo.
    Ele é ser usado só quando a thread já está dentro da seção crítica.

    Exemplo:
    A liberação RANDOM já é chamada dentro do FirstFit.
    O FirstFit já roda dentro de alocarFirstFit().
    Então o semáforo já foi adquirido.
    */
    public boolean liberarPorIdInterno(int id) {
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

    /*
     Este método precisa ser protegido porque várias threads podem tentar alocar memória ao mesmo tempo.
     
     Dentro do FirstFit podem acontecer várias operações perigosas se forem executadas por múltiplas threads simultaneamente:
     
     Buscar espaço livre na heap;
     Gerar um novo ID;
     Escrever o ID dentro do vetor da heap;
     Registrar o bloco na tabela de alocações;
     Liberar blocos aleatórios, se não houver espaço;
     Compactar a heap, se houver fragmentação.
     
     usamos o semáforo para permitir que apenas uma thread por vez execute essa parte do código.
     */

        boolean entrou = semaforo.adquirir();

        if (!entrou) {
            System.err.printf("\nNão foi possível acessar a heap. Alocação cancelada.\n");
            return -1;
        }

        try {
            return new FirstFit(heap, this).alocar(tamanhoBytes);
        }   finally {
            semaforo.liberar();
            }
    }

    /*
    Método para chamar a liberação aleatória
 
    Ele adquire o semáforo antes de liberar blocos da heap.
    Assim, nenhuma outra thread consegue alocar, liberar ou compactar ao mesmo tempo.
 */
    public void liberarAleatorio() {
        boolean entrou = semaforo.adquirir();

        if (!entrou) {
            System.err.printf("\nERRO. Não foi possível acessar a heap para realizar a liberação aleatória\n");
            return;
        }

        try {
            liberarAleatorioInterno();
        } finally {
            semaforo.liberar();
        }
    }

    /*
    Método interno de liberação aleatória.
 
    Este método não pega o semáforo.
    Ele é usado só quando a thread já está dentro da seção crítica da heap.
 
    Exemplo:
    O FirstFit já é chamado dentro de alocarFirstFit(), e alocarFirstFit() já protege a heap com semáforo.
    */
    public void liberarAleatorioInterno() {
        new Libera(heap, this).liberar();
    }

    /*
    Método para chamar a compactação diretamente.

    Ele pega o semáforo antes de compactar a heap.
    Assim, nenhuma outra thread consegue alocar, liberar ou compactar ao mesmo tempo.
    */
    public void compactar() {
        boolean entrou = semaforo.adquirir();

        if (!entrou) {
            System.err.printf("\nERRO. Não foi possível acessar a heap para realizar a compactação.\n");
            return;
        }

        try {
            compactarInterno();
        }   finally {
                semaforo.liberar();
            }
    }

    /*
    Método interno de compactação.

    Este método não pega o semáforo.
    Ele deve ser usado apenas quando a thread já está dentro da seção crítica da heap.

    Exemplo:
    O FirstFit já roda dentro de alocarFirstFit().
    Então, dentro do FirstFit, usamos compactarInterno().
    */
    public void compactarInterno() {
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
        System.out.printf("Livre | Início: %d | Fim: %d | Slots: %d | Bytes: %d\n", inicio, inicio + tamanhoSlots - 1, tamanhoSlots, tamanhoSlots * 4);
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