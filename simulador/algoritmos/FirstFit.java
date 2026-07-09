package simulador.algoritmos;

import simulador.heap.GerenciadorHeap;
import simulador.heap.Heap;

public class FirstFit {
    private final Heap heap;
    private final GerenciadorHeap gerenciador;

    public FirstFit(Heap heap, GerenciadorHeap gerenciador) {
        this.heap = heap;
        this.gerenciador = gerenciador;
    }

     // Procura o primeiro espaço livre contíguo com tamanho suficiente.

     // Retorna:
     // índice inicial do espaço encontrado;
     // -1 se não encontrar espaço contíguo suficiente.
    public int buscarEspaco(int tamanhoSlots) {
        int[] memoria = heap.getMemoria();

        int inicioLivre = -1;
        int contadorLivre = 0;

        for (int i = 0; i < memoria.length; i++) {
            if (memoria[i] == 0) {

                if (inicioLivre == -1) {
                    inicioLivre = i;
                }

                contadorLivre++;

                if (contadorLivre >= tamanhoSlots) {
                    return inicioLivre;
                }

            } else {
                inicioLivre = -1;
                contadorLivre = 0;
            }
        }

        return -1;
    }

     // Tenta alocar um bloco na heap usando First Fit.
    
     // Retorna:
     // ID do bloco, se conseguir alocar;
     // -1, se não conseguir alocar.
     
     // Este método deve ser chamado dentro de GerenciadorHeap.alocarFirstFit(),
     // pois lá existe o semáforo que protege a heap.

    public int alocar(int tamanhoBytes) {
        if (tamanhoBytes <= 0) {
            throw new IllegalArgumentException("O tamanho em bytes deve ser maior que zero");
        }

         // Cada slot da heap representa 4 bytes.
         // Então precisamos converter o tamanho solicitado em bytes
         // para quantidade de slots.
        
        int tamanhoSlots = heap.converterBytesParaSlots(tamanhoBytes);

        if (tamanhoSlots > heap.getTotalSlots()) {
            System.out.printf("\nREQUISIÇÃO REJEITADA: tamanho solicitado (%d bytes) é maior que a heap inteira (%d bytes).\n", tamanhoBytes, heap.getTotalBytes());

            return -1;
        }

         // tenta encontrar espaço livre contíguo
         
        int inicio = buscarEspaco(tamanhoSlots);

         // Se não encontrou espaço contíguo, precisamos lidar com
         // fragmentação ou heap cheia.
         
        if (inicio == -1) {
            int slotsLivres = gerenciador.contarSlotsLivresPelaHeap();

             // CASO 1:
             // Existe espaço livre suficiente no total, mas ele está espalhado.
             // fragmentação externa, então devemos compactar a heap
            if (slotsLivres >= tamanhoSlots) {
                System.out.printf("\nNão há espaço contíguo para %d slots, mas existem %d slots livres espalhados. Compactando\n", tamanhoSlots, slotsLivres);

                gerenciador.compactarInterno();
            }   else {

                 // CASO 2:
                 // Não existe espaço livre suficiente. Isso é heap cheia ou quase cheia.

                 // chamar liberação RANDOM;
                 // compactar;
                 // tentar alocar novamente.
        
                    System.out.printf("\nHeap sem espaço suficiente para %d slots. Slots livres atuais: %d. Acionando liberação RANDOM\n", tamanhoSlots, slotsLivres);

                 // Como o projeto usa semáforos e não possui uma thread liberadora separada, a thread alocadora não fica esperando quando a heap está cheia.
                 // Em vez disso, ela mesma aciona a liberação RANDOM imediatamente.
                 // Isso evita deadlock, porque se a thread ficasse esperando, não haveria outra thread responsável por liberar a heap.

                    gerenciador.liberarAleatorioInterno();

                    System.out.printf("\nCompactando após liberação RANDOM\n");

                    gerenciador.compactarInterno();
                }

             // Depois da compactação ou liberação, tenta buscar espaço novamente.
            
            inicio = buscarEspaco(tamanhoSlots);

             // Se ainda não encontrou espaço, a requisição é rejeitada.
             
             // Isso pode acontecer se
             // a heap não tiver blocos suficientes para liberar;
             // mesmo após liberar 30%, ainda não existir espaço para essa requisição;
             // a requisição for muito grande para o estado atual da heap.
            
            if (inicio == -1) {
                System.out.printf("\nREQUISIÇÃO REJEITADA: sem espaço após liberação e compactação\n");
                return -1;
            }
        }

         // Se chegou aqui, encontramos espaço.
         // Agora geramos um ID exclusivo para o bloco.

        int id = gerenciador.gerarNovoId();

         // Escreve o ID em todos os slots ocupados pela requisição.
        heap.escrever(inicio, tamanhoSlots, id);

         // Registra o bloco na tabela de alocações.
        gerenciador.registrarBlocoAlocado(id, inicio, tamanhoBytes, tamanhoSlots);

        return id;
    }
}