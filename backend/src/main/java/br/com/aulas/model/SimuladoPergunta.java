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
@Table(name = "simuladosperguntas")
public class SimuladoPergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsimuladopergunta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idsimulado", nullable = false)
    private Simulado simulado;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<SimuladoOpcao> opcoes = new ArrayList<>();

    public Integer getId() { return id; }
    public Simulado getSimulado() { return simulado; }
    public void setSimulado(Simulado simulado) { this.simulado = simulado; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public List<SimuladoOpcao> getOpcoes() { return opcoes; }

    public void substituirOpcoes(List<SimuladoOpcao> novasOpcoes) {
        opcoes.clear();
        for (SimuladoOpcao opcao : novasOpcoes) {
            opcao.setPergunta(this);
            opcoes.add(opcao);
        }
    }
}
