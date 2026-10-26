package br.com.aulas.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulados")
public class Simulado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsimulado")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idaulatema", nullable = false)
    private AulaTema tema;

    @Column(nullable = false, length = 180)
    private String titulo;

    @OneToMany(mappedBy = "simulado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<SimuladoPergunta> perguntas = new ArrayList<>();

    public Integer getId() { return id; }
    public AulaTema getTema() { return tema; }
    public void setTema(AulaTema tema) { this.tema = tema; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public List<SimuladoPergunta> getPerguntas() { return perguntas; }

    public void substituirPerguntas(List<SimuladoPergunta> novasPerguntas) {
        perguntas.clear();
        for (SimuladoPergunta pergunta : novasPerguntas) {
            pergunta.setSimulado(this);
            perguntas.add(pergunta);
        }
    }
}
