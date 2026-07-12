package simulador.threads;

public class Requisicao {
    private final int numero;
    private final int tamanhoBytes;
    private final boolean fim;

    public Requisicao(int numero, int tamanhoBytes) {
        this.numero = numero;
        this.tamanhoBytes = tamanhoBytes;
        this.fim = false;
    }

    private Requisicao() {
        this.numero = -1;
        this.tamanhoBytes = 0;
        this.fim = true;
    }

    public static Requisicao criarRequisicaoFim() {
        return new Requisicao();
    }

    public int getNumero() {
        return numero;
    }

    public int getTamanhoBytes() {
        return tamanhoBytes;
    }

    public boolean isFim() {
        return fim;
    }

    @Override
    public String toString() {
        if (fim) {
            return "Requisição de encerramento";
        }

        return "Requisição " + numero + " | Tamanho: " + tamanhoBytes + " bytes";
    }
}