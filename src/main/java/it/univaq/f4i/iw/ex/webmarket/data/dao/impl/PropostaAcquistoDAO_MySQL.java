package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaAcquistoDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.PropostaAcquistoProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PropostaAcquistoDAO_MySQL extends DAO implements PropostaAcquistoDAO {

    // Query SQL precompilate
    private PreparedStatement sPropostaByID, sProposteByRichiesta, sAllProposte, iProposta, uProposta, dProposta;

    public PropostaAcquistoDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            // Inizializzazione delle prepared statements con le query SQL
            sPropostaByID = connection.prepareStatement("SELECT * FROM proposta_acquisto WHERE ID = ?");
            sProposteByRichiesta = connection.prepareStatement("SELECT * FROM proposta_acquisto WHERE richiesta_id = ?");
            sAllProposte = connection.prepareStatement("SELECT * FROM proposta_acquisto");
            //TODO: mi da l'errore sul Statement.RETURN_generated_KEYS
            // iProposta = connection.prepareStatement("INSERT INTO proposta_acquisto (produttore, prodotto, codice, prezzo, URL, note, stato, motivazione, richiesta_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uProposta = connection.prepareStatement("UPDATE proposta_acquisto SET produttore=?, prodotto=?, codice=?, prezzo=?, URL=?, note=?, stato=?, motivazione=?, richiesta_id=? WHERE ID=?");
            dProposta = connection.prepareStatement("DELETE FROM proposta_acquisto WHERE ID=?");
        } catch (SQLException ex) {
            throw new DataException("Errore durante l'inizializzazione del data layer per le proposte d'acquisto", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        try {
            // Chiusura delle prepared statements
            sPropostaByID.close();
            sProposteByRichiesta.close();
            sAllProposte.close();
            iProposta.close();
            uProposta.close();
            dProposta.close();
        } catch (SQLException ex) {
            // Ignora eccezione di chiusura
        }
        super.destroy();
    }

    @Override
    public PropostaAcquisto createPropostaAcquisto() {
        return new PropostaAcquistoProxy(getDataLayer());
    }

    // Metodo helper per creare un oggetto PropostaAcquistoProxy da un ResultSet
    private PropostaAcquistoProxy createPropostaAcquisto(ResultSet rs) throws DataException {
        try {
            PropostaAcquistoProxy p = (PropostaAcquistoProxy) createPropostaAcquisto();
            p.setKey(rs.getInt("ID"));
            p.setProduttore(rs.getString("produttore"));
            p.setProdotto(rs.getString("prodotto"));
            p.setCodice(rs.getString("codice"));
            p.setPrezzo(rs.getFloat("prezzo"));
            p.setUrl(rs.getString("URL"));
            p.setNote(rs.getString("note"));
            //
            p.setStatoProposta(StatoProposta.valueOf(rs.getString("stato")));
            p.setMotivazione(rs.getString("motivazione"));
            // Recupera l'oggetto RichiestaOrdine tramite il suo ID
        // RichiestaOrdine richiesta = richiestaOrdineDAO.getRichiestaOrdine(rs.getInt("richiesta_id"));
        // p.setRichiestaOrdine(richiesta);
            // p.setRichiestaOrdine(rs.getInt("richiesta_id")); 
            return p;
        } catch (SQLException ex) {
            throw new DataException("Impossibile creare l'oggetto proposta d'acquisto dal ResultSet", ex);
        }
    }

    @Override
    public PropostaAcquisto getPropostaAcquisto(int proposta_key) throws DataException {
        PropostaAcquisto p = null;
        if (dataLayer.getCache().has(PropostaAcquisto.class, proposta_key)) {
            p = dataLayer.getCache().get(PropostaAcquisto.class, proposta_key);
        } else {
            try {
                sPropostaByID.setInt(1, proposta_key);
                try (ResultSet rs = sPropostaByID.executeQuery()) {
                    if (rs.next()) {
                        p = createPropostaAcquisto(rs);
                        dataLayer.getCache().add(PropostaAcquisto.class, p);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Impossibile caricare la proposta d'acquisto tramite ID", ex);
            }
        }
        return p;
    }

    @Override
    public List<PropostaAcquisto> getProposteAcquistoByRichiesta(int richiesta_id) throws DataException {
        List<PropostaAcquisto> proposte = new ArrayList<>();
        try {
            sProposteByRichiesta.setInt(1, richiesta_id);
            try (ResultSet rs = sProposteByRichiesta.executeQuery()) {
                while (rs.next()) {
                    proposte.add(createPropostaAcquisto(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare le proposte d'acquisto per la richiesta specificata", ex);
        }
        return proposte;
    }

    @Override
    public List<PropostaAcquisto> getAllProposteAcquisto() throws DataException {
        List<PropostaAcquisto> proposte = new ArrayList<>();
        try (ResultSet rs = sAllProposte.executeQuery()) {
            while (rs.next()) {
                proposte.add(createPropostaAcquisto(rs));
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare tutte le proposte d'acquisto", ex);
        }
        return proposte;
    }

    @Override
    public void storePropostaAcquisto(PropostaAcquisto proposta) throws DataException {
        try {
            if (proposta.getKey() != null && proposta.getKey() > 0) {
                // Se la proposta è un proxy e non è stata modificata, salta l'aggiornamento
                if (proposta instanceof DataItemProxy && !((DataItemProxy) proposta).isModified()) {
                    return;
                }
                // Aggiorna la proposta d'acquisto esistente
                uProposta.setString(1, proposta.getProduttore());
                uProposta.setString(2, proposta.getProdotto());
                uProposta.setString(3, proposta.getCodice());
                uProposta.setDouble(4, proposta.getPrezzo());
                uProposta.setString(5, proposta.getUrl());
                uProposta.setString(6, proposta.getNote());
                uProposta.setString(7, proposta.getStatoProposta().toString());
                uProposta.setString(8, proposta.getMotivazione());
                uProposta.setInt(9, proposta.getRichiestaOrdine().getId());
                uProposta.setInt(10, proposta.getKey());
                uProposta.executeUpdate();
            } else {
                // Inserisce una nuova proposta d'acquisto nel database
                iProposta.setString(1, proposta.getProduttore());
                iProposta.setString(2, proposta.getProdotto());
                iProposta.setString(3, proposta.getCodice());
                iProposta.setDouble(4, proposta.getPrezzo());
                iProposta.setString(5, proposta.getUrl());
                iProposta.setString(6, proposta.getNote());
                iProposta.setString(7, proposta.getStatoProposta().toString());
                iProposta.setString(8, proposta.getMotivazione());
                iProposta.setInt(9, proposta.getRichiestaOrdine().getId());
                if (iProposta.executeUpdate() == 1) {
                    try (ResultSet keys = iProposta.getGeneratedKeys()) {
                        if (keys.next()) {
                            proposta.setKey(keys.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile salvare la proposta d'acquisto", ex);
        }
    }

    @Override
    public void deletePropostaAcquisto(int proposta_key) throws DataException {
        try {
            dProposta.setInt(1, proposta_key);
            dProposta.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Impossibile eliminare la proposta d'acquisto", ex);
        }
    }

    @Override
    public List<PropostaAcquisto> getProposteByUtente(int utente_key) throws DataException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProposteByUtente'");
    }

    @Override
    public List<PropostaAcquisto> getProposteByOrdine(int ordine_key) throws DataException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProposteByOrdine'");
    }

    @Override
    public void inviaPropostaAcquisto(PropostaAcquisto proposta) throws DataException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inviaPropostaAcquisto'");
    }
}
