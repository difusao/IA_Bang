package com.ag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AlgoritmoGenetico {
    private int tamanhoPopulacao;
    private List<Item> populacao = new ArrayList();
    private int geracao;
    private Item melhorSolucao;
    private List<Item> melhoresCromossomos = new ArrayList();

    public AlgoritmoGenetico(int tamanhoPopulacao) {
        this.tamanhoPopulacao = tamanhoPopulacao;
    }

    public void inicializaPopulacao(List espacos, List valores, Double limiteEspacos) {
        for (int i = 0; i < this.tamanhoPopulacao; i++) {
            this.populacao.add(new Item(espacos, valores, limiteEspacos));
        }
        this.melhorSolucao = this.populacao.get(0);
    }

    public void ordenaPopulacao() {
        Collections.sort(this.populacao);
    }

    public void melhorItem(Item Item) {
        if (Item.getNotaAvaliacao() > this.melhorSolucao.getNotaAvaliacao()) {
            this.melhorSolucao = Item;
        }
    }

    public Double somaAvaliacoes() {
        Double soma = 0.0;
        for (Item Item: this.populacao) {
            soma += Item.getNotaAvaliacao();
        }
        return soma;
    }

    public int selecionaPai(Double somaAvaliacao) {
        int pai = -1;
        Double valorSorteado = Math.random() * somaAvaliacao;
        Double soma = 0.0;
        int i = 0;
        while (i < this.populacao.size() && soma < valorSorteado) {
            soma += this.populacao.get(i).getNotaAvaliacao();
            pai += 1;
            i += 1;
        }
        return pai;
    }

    public void visualizaGeracao() {
        Item melhor = this.populacao.get(0);
        this.melhoresCromossomos.add(melhor);

        //System.out.println("G: " + melhor.getGeracao() +
        //        " Valor: " + melhor.getNotaAvaliacao() +
        //        " Espaço: " + melhor.getEspacoUsado() +
        //        " Cromossomo: " + melhor.getCromossomo());
        //System.out.printf(Locale.US, "Gen: %03d Space: %f Value: %f Cromoss: %s%n", melhor.getGeracao(), melhor.getNotaAvaliacao(), melhor.getEspacoUsado(), this.melhorSolucao.getCromossomo());
    }

    public List resolver(Double taxaMutacao, int numeroGeracoes, List espacos,
                         List valores, Double limiteEspacos) {

        this.inicializaPopulacao(espacos, valores, limiteEspacos);
        for (Item Item: this.populacao) {
            Item.avaliacao();
        }
        this.ordenaPopulacao();
        this.visualizaGeracao();

        for (int geracao = 0; geracao < numeroGeracoes; geracao++) {
            Double somaAvaliacao = this.somaAvaliacoes();
            List<Item> novaPopulacao = new ArrayList();

            for (int i = 0; i < this.populacao.size() / 2; i++) {
                int pai1 = this.selecionaPai(somaAvaliacao);
                int pai2 = this.selecionaPai(somaAvaliacao);

                List<Item> filhos = this.getPopulacao().get(pai1).crossover(this.getPopulacao().get(pai2));
                novaPopulacao.add(filhos.get(0).mutacao(taxaMutacao));
                novaPopulacao.add(filhos.get(1).mutacao(taxaMutacao));
            }

            this.setPopulacao(novaPopulacao);
            for (Item Item: this.getPopulacao()) {
                Item.avaliacao();
            }
            this.ordenaPopulacao();
            this.visualizaGeracao();
            Item melhor = this.populacao.get(0);
            this.melhorItem(melhor);

        }

        //System.out.println("Melhor solução G -> " + this.melhorSolucao.getGeracao() +
        //        " Valor: " + this.melhorSolucao.getNotaAvaliacao() +
        //        " Espaço: " + this.melhorSolucao.getEspacoUsado() +
        //        " Cromossomo: " + this.melhorSolucao.getCromossomo());

        System.out.printf(Locale.US, "%nBest Generation: %03d Space Total: %f Value Total: %f Cromoss: %s%n%n", this.melhorSolucao.getGeracao(), this.melhorSolucao.getNotaAvaliacao(), this.melhorSolucao.getEspacoUsado(), this.melhorSolucao.getCromossomo());

        return this.melhorSolucao.getCromossomo();
    }

    public List<Item> getMelhoresCromossomos() {
        return melhoresCromossomos;
    }

    public void setMelhoresCromossomos(List<Item> melhoresCromossomos) {
        this.melhoresCromossomos = melhoresCromossomos;
    }

    public int getTamanhoPopulacao() {
        return tamanhoPopulacao;
    }

    public void setTamanhoPopulacao(int tamanhoPopulacao) {
        this.tamanhoPopulacao = tamanhoPopulacao;
    }

    public List<Item> getPopulacao() {
        return populacao;
    }

    public void setPopulacao(List<Item> populacao) {
        this.populacao = populacao;
    }

    public int getGeracao() {
        return geracao;
    }

    public void setGeracao(int geracao) {
        this.geracao = geracao;
    }

    public Item getMelhorSolucao() {
        return melhorSolucao;
    }

    public void setMelhorSolucao(Item melhorSolucao) {
        this.melhorSolucao = melhorSolucao;
    }
}
