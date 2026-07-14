package simulador.threads;

public class Requisicao {
    private final int numero; //numero da requisição
    private final int tamanhoBytes; //tamanho que vai ser alocado
    private final boolean fim; //diz se é uma requisição normal ou de fim

    // requisições normais
    public Requisicao(int numero, int tamanhoBytes) {
        this.numero = numero;
        this.tamanhoBytes = tamanhoBytes;
        this.fim = false;
    }

    // requisição de fim, o -1 e o 0 são pra indicar que ela é uma requisição de fim
    private Requisicao() {
        this.numero = -1;
        this.tamanhoBytes = 0;
        this.fim = true;
    }

    //cria a requisição de fim
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
            return "Requisição de encerramento"; //mensagem requisição de fim
        }

        return "Requisição " + numero + " | Tamanho: " + tamanhoBytes + " bytes"; //mensagem requisição normal
    }
}