package simulador.heap;

public class BlocoAlocado {
    private int id;
    private int inicio;
    private int tamanhoSolicitadoBytes;
    private int tamanhoSlots;

    public BlocoAlocado(int id, int inicio, int tamanhoSolicitadoBytes, int tamanhoSlots) {
        this.id = id;
        this.inicio = inicio;
        this.tamanhoSolicitadoBytes = tamanhoSolicitadoBytes;
        this.tamanhoSlots = tamanhoSlots;
    }

    public int getId() {
        return id;
    }

    public int getInicio() {
        return inicio;
    }

    public void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public int getTamanhoSolicitadoBytes() {
        return tamanhoSolicitadoBytes;
    }

    public int getTamanhoSlots() {
        return tamanhoSlots;
    }

    public int getFim() {
        return inicio + tamanhoSlots - 1;
    }

    public int getTamanhoRealBytes() {
        return tamanhoSlots * 4;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Início: " + inicio + " | Fim: " + getFim() + " | Slots: " + tamanhoSlots + " | Bytes solicitados: " + tamanhoSolicitadoBytes + " | Bytes ocupados na heap: " + getTamanhoRealBytes();
    }
}
