package simulador.threads;

public class Requisicao {
     // Cada requisição representa um pedido de alocação de memória.
    
     // Exemplo:
     // Requisição 1 pede 128 bytes.
     // Requisição 2 pede 64 bytes.
    private final int numero;
    private final int tamanhoBytes;

     // é usado para avisar as threads alocadoras que não existem mais requisições para processar.
    private final boolean fim;

    public Requisicao(int numero, int tamanhoBytes) {
        this.numero = numero;
        this.tamanhoBytes = tamanhoBytes;
        this.fim = false;
    }

     //usado para criar uma requisição de fim.
    private Requisicao() {
        this.numero = -1;
        this.tamanhoBytes = 0;
        this.fim = true;
    }

     // usado para criar uma requisição de encerramento.
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