package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.ator.Ator;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

// Tabela puramente associativa (elenco do filme) — vive no pacote "filme" por não ter
// ciclo de vida/feature próprios.
@Entity
@Table(name = "filme_ator")
public class FilmeAtor {

    @EmbeddedId
    private FilmeAtorId id = new FilmeAtorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idFilme")
    @JoinColumn(name = "id_filme")
    private Filme filme;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAtor")
    @JoinColumn(name = "id_ator")
    private Ator ator;

    public FilmeAtorId getId() {
        return id;
    }

    public void setId(FilmeAtorId id) {
        this.id = id;
    }

    public Filme getFilme() {
        return filme;
    }

    public void setFilme(Filme filme) {
        this.filme = filme;
    }

    public Ator getAtor() {
        return ator;
    }

    public void setAtor(Ator ator) {
        this.ator = ator;
    }
}
