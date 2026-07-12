package simulador.heap;

public class Heap {
    private final int[] memoria;
    private final int totalSlots;
    private final int totalBytes;

    public Heap(int tamanhoKB) {
        this.totalBytes = tamanhoKB * 1024;
        this.totalSlots = totalBytes / 4;
        this.memoria = new int[totalSlots];
    }

    public int converterBytesParaSlots(int bytes) {
        return (int) Math.ceil((double) bytes / 4);
    }

    public int converterSlotsParaBytes(int slots) {
        return slots * 4;
    }

    public void escrever(int inicio, int tamanhoSlots, int id) {
        for (int i = inicio; i < inicio + tamanhoSlots; i++) {
            memoria[i] = id;
        }
    }

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

        for (int i = 0; i < exibir; i++) {
            System.out.printf("[%d]", memoria[i]);
        }

        System.out.printf("\n0 = livre | número = ID da requisição ocupando o slot");
        System.out.printf("\nTotal de slots: %d | Total de bytes: %d\n", totalSlots, totalBytes);
    }
}