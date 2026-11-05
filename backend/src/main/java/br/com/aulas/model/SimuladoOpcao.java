package br.com.aulas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "simuladosopcoes")
public class SimuladoOpcao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsimuladoopcao")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idsimuladopergunta", nullable = false)
    private SimuladoPergunta pergunta;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private boolean correta;

    public Integer getId() { return id; }
    public SimuladoPergunta getPergunta() { return pergunta; }
    public void setPergunta(SimuladoPergunta pergunta) { this.pergunta = pergunta; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public boolean isCorreta() { return correta; }
    public void setCorreta(boolean correta) { this.correta = correta; }
}
