package simulador.main;

public class TesteDeadlockStarvation {

    public static void main(String[] args) {

        System.out.printf("\nTESTE DE DEADLOCK E STARVATION\n");
        
        testarConfiguracao(2);
        testarConfiguracao(4);
        testarConfiguracao(8);

        System.out.printf("\nTESTE FINALIZADO\n");
    }

    private static void testarConfiguracao(int numeroThreads) {

        int tamanhoHeapKB = 128;
        int tamanhoMinimoBytes = 16;
        int tamanhoMaximoBytes = 1024;
        int totalRequisicoes = 500;

        long tempoLimiteMs = 30000; // 30 segundos

        System.out.printf("\nIniciando teste com %d threads", numeroThreads);
        System.out.printf("\nHeap: %d KB", tamanhoHeapKB);
        System.out.printf("\nRequisições: %d", totalRequisicoes);
        System.out.printf("\nFaixa: %d a %d bytes", tamanhoMinimoBytes, tamanhoMaximoBytes);
        System.out.printf("\nTempo limite: %d ms\n", tempoLimiteMs);
        
        Paralelo simulador = new Paralelo(tamanhoHeapKB, tamanhoMinimoBytes, tamanhoMaximoBytes, totalRequisicoes, numeroThreads);

         // uma thread criada só para executar o simulador.
         // Assim usamos o join(tempoLimiteMs), que espera no máximo o tempo definido (30 seg).
        Thread threadTeste = new Thread(new Runnable() {
            @Override
            public void run() {
                simulador.executar();
            }
        });

        long inicio = System.currentTimeMillis();
        threadTeste.start();

        try {
             // Espera a execução terminar, mas somente até o limite.
            threadTeste.join(tempoLimiteMs);

        } catch (InterruptedException e) {
            System.err.printf("\nERRO. O teste foi interrompido.\n");
            Thread.currentThread().interrupt();
            return;
        }

        long fim = System.currentTimeMillis();
        long tempoTotal = fim - inicio;

         // Se a thread ainda estiver viva após o tempo limite, provavelmente o programa travou.
        if (threadTeste.isAlive()) {
            System.out.printf("\nERRO. Possível deadlock detectado com %d threads.", numeroThreads);
            System.out.printf("\nO teste ultrapassou o limite de %d ms.\n", tempoLimiteMs);

             // Tenta interromper a execução para não deixar o teste preso.
            threadTeste.interrupt();

        } else {
            System.out.printf("\nOK. Execução com %d threads terminou sem deadlock.", numeroThreads);
            System.out.printf("\nTempo total do teste: %d ms\n", tempoTotal);
        }
    }
}