package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.framework.data.DataItemImpl;



public class PropostaAcquistoImpl extends DataItemImpl<Integer> implements PropostaAcquisto {
    private int id;
    private String produttore;
    private String prodotto;
    private String codice;
    private double prezzo;
    private String url;
    private String note;
    private StatoProposta stato;
    private String motivazione;
    private RichiestaOrdine richiestaOrdine;

    // Costruttori
    public PropostaAcquistoImpl() {
        super();
        produttore = "";
        prodotto = "";
        codice = "";
        prezzo = 0;
        url = "";
        note = "";
        stato = null;
        motivazione = "";
        richiestaOrdine = null;
    }

    public PropostaAcquistoImpl(int id, String produttore, String prodotto, String codice, float prezzo, String url, String note, StatoProposta stato, String motivazione, RichiestaOrdine richiestaOrdine) {
        this.id = id;
        this.produttore = produttore;
        this.prodotto = prodotto;
        this.codice = codice;
        this.prezzo = prezzo;
        this.url = url;
        this.note = note;
        this.stato = stato;
        this.motivazione = motivazione;
        this.richiestaOrdine = richiestaOrdine;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getProduttore() {
        return produttore;
    }

    @Override
    public void setProduttore(String produttore) {
        this.produttore = produttore;
    }

    @Override
    public String getProdotto() {
        return prodotto;
    }

    @Override
    public void setProdotto(String prodotto) {
        this.prodotto = prodotto;
    }

    @Override
    public String getCodice() {
        return codice;
    }

    @Override
    public void setCodice(String codice) {
        this.codice = codice;
    }

    @Override
    public double getPrezzo() {
        return prezzo;
    }

    @Override
    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public StatoProposta getStatoProposta() {
        return stato;
    }

    @Override
    public void setStatoProposta(StatoProposta stato) {
        this.stato = stato;
    }

    @Override
    public String getMotivazione() {
        return motivazione;
    }

    @Override
    public void setMotivazione(String motivazione) {
        this.motivazione = motivazione;
    }

    @Override
    public RichiestaOrdine getRichiestaOrdine() {
        return richiestaOrdine;
    }

    @Override
    public void setRichiestaOrdine(RichiestaOrdine richiestaOrdine) {
        this.richiestaOrdine = richiestaOrdine;
    }
}
