package simulador.heap;

// cada posição do vetor equivale a 4 bytes (tamanho do um int em Java)
// Exemplo: heap de 1 KB = 1024 bytes / 4 = 256 slots
public class Heap {
    private final int[] memoria;  // representa a memória da heap, onde cada posição é um slot de 4 bytes
    private final int totalSlots;
    private final int totalBytes;

    // cria uma heap com o tamanho escolhido pelo usuário
    public Heap(int tamanhoKB) {
        // converte KB para bytes: 1 KB = 1024 bytes
        this.totalBytes = tamanhoKB * 1024;
    
        // converte bytes para slots: cada int = 4 bytes
        this.totalSlots = totalBytes / 4;
        this.memoria = new int[totalSlots];
    }

     // converte um tamanho em bytes para o número de slots necessários
     // math.ceil arredonda para cima porque um slot não pode ser dividido entre dois blocos
    public int converterBytesParaSlots(int bytes) {
        return (int) Math.ceil((double) bytes / 4);
    }

     //Converte um número de slots para o tamanho equivalente em bytes
    public int converterSlotsParaBytes(int slots) {
        return slots * 4;
    }


    // Escreve o ID da requisição em todos os slots ocupados pelo bloco.
    public void escrever(int inicio, int tamanhoSlots, int id) {
        for (int i = inicio; i < inicio + tamanhoSlots; i++) {
            memoria[i] = id;
        }
    }

    //zera os slots ocupados pelo bloco, marcando-os como livres (0)
    public void liberar(int inicio, int tamanhoSlots) {
        for (int i = inicio; i < inicio + tamanhoSlots; i++) {
            memoria[i] = 0;
        }
    }



    public int[] getMemoria() {
        return memoria;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void imprimirEstado(int limite) {
        int exibir = Math.min(limite, totalSlots);

        System.out.printf("\n=== Estado da Heap (primeiros %d slots) ===", exibir);
        System.out.printf("\nSlots: ");
        for (int x = 0; x < exibir; x++) {
            System.out.printf("[%d]", memoria[x]);
        }

        System.out.printf("\n0 = livre | número = ID da requisição ocupando o slot");
        System.out.printf("\nTotal de slots: %d | Total de bytes: %d\n", totalSlots, totalBytes);
    }
}