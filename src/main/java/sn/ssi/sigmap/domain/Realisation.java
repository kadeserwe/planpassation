package sn.ssi.sigmap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A Realisation.
 */
@Entity
@Table(name = "realisation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Realisation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "libelle")
    private String libelle;

    @NotNull
    @Column(name = "date_attribution", nullable = false)
    private LocalDate dateAttribution;

    @NotNull
    @Column(name = "delaiexecution", nullable = false)
    private Integer delaiexecution;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = "realisations", allowSetters = true)
    private PlanPassation planPassation;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public Realisation libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public LocalDate getDateAttribution() {
        return dateAttribution;
    }

    public Realisation dateAttribution(LocalDate dateAttribution) {
        this.dateAttribution = dateAttribution;
        return this;
    }

    public void setDateAttribution(LocalDate dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public Integer getDelaiexecution() {
        return delaiexecution;
    }

    public Realisation delaiexecution(Integer delaiexecution) {
        this.delaiexecution = delaiexecution;
        return this;
    }

    public void setDelaiexecution(Integer delaiexecution) {
        this.delaiexecution = delaiexecution;
    }

    public PlanPassation getPlanPassation() {
        return planPassation;
    }

    public Realisation planPassation(PlanPassation planPassation) {
        this.planPassation = planPassation;
        return this;
    }

    public void setPlanPassation(PlanPassation planPassation) {
        this.planPassation = planPassation;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Realisation)) {
            return false;
        }
        return id != null && id.equals(((Realisation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Realisation{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            ", dateAttribution='" + getDateAttribution() + "'" +
            ", delaiexecution=" + getDelaiexecution() +
            "}";
    }
}
