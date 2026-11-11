package br.edu.ufvjm.locadora.filme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FilmeAtorId implements Serializable {

    @Column(name = "id_filme")
    private Integer idFilme;

    @Column(name = "id_ator")
    private Integer idAtor;

    public FilmeAtorId() {
    }

    public FilmeAtorId(Integer idFilme, Integer idAtor) {
        this.idFilme = idFilme;
        this.idAtor = idAtor;
    }

    public Integer getIdFilme() {
        return idFilme;
    }

    public void setIdFilme(Integer idFilme) {
        this.idFilme = idFilme;
    }

    public Integer getIdAtor() {
        return idAtor;
    }

    public void setIdAtor(Integer idAtor) {
        this.idAtor = idAtor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilmeAtorId other)) {
            return false;
        }
        return Objects.equals(idFilme, other.idFilme) && Objects.equals(idAtor, other.idAtor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFilme, idAtor);
    }
}
