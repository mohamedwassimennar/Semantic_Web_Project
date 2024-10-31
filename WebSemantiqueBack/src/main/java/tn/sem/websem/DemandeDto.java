package tn.sem.websem;

import java.util.Date;

public class DemandeDto {
    private String typeNouritture;
    private Float quantiteDemande;
    private java.util.Date Date;

    public String getTypeNouritture() {
        return typeNouritture;
    }

    public void setTypeNouritture(String typeNouritture) {
        this.typeNouritture = typeNouritture;
    }

    public Float getQuantiteDemande() {
        return quantiteDemande;
    }

    public void setQuantiteDemande(Float quantiteDemande) {
        this.quantiteDemande = quantiteDemande;
    }

    public java.util.Date getDate() {
        return Date;
    }

    public void setDate(java.util.Date date) {
        Date = date;
    }
}
