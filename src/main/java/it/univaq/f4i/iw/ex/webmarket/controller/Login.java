package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class Login extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, TemplateManagerException {
        TemplateResult result = new TemplateResult(getServletContext());
        request.setAttribute("referrer", request.getParameter("referrer"));
        result.activate("login.ftl.html", request, response);

//        //esempio di creazione utente
//        //create user example
//        try {
//            User u = ((NewspaperDataLayer) request.getAttribute("datalayer")).getUserDAO().createUser();
//            u.setUsername("a");
//            u.setPassword(SecurityHelpers.getPasswordHashPBKDF2("p"));
//            ((NewspaperDataLayer) request.getAttribute("datalayer")).getUserDAO().storeUser(u);
//        } catch (DataException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
//            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    //nota: usente di default nel database: nome a, password p
    //note: default user in the database: name: a, password p
    private void action_login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("email");
        String password = request.getParameter("password");

        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                //System.out.println("email: " + username);
                System.out.println("ciao "+username);
                
                Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtenteByEmail(username);
                
               // if (u != null && SecurityHelpers.checkPasswordHashPBKDF2(password, u.getPassword())) {
                if (u != null && password.equals(u.getPassword())) {
                    //se la validazione ha successo
                    //if the identity validation succeeds
                    System.out.println("sono dentro, l'utente è: "+u.getEmail());
                    SecurityHelpers.createSession(request, username, u.getKey());
                    //se è stato trasmesso un URL di origine, torniamo a quell'indirizzo
                    //if an origin URL has been transmitted, return to it
                    System.out.println("tipologia "+u.getTipologiaUtenteId());
                    String redirectPage;
                    redirectPage = switch (u.getTipologiaUtenteId()) {
    
                        case Ordinante -> "homepageordinante";
                        case Tecnico -> "homepagetecnico";
                        case Amministratore -> "homepageadmin";
                        default -> "login";
                    };
                    
                    if (request.getParameter("referrer") != null) {
                        //response.sendRedirect(request.getParameter("referrer"));
                        response.sendRedirect(request.getParameter(redirectPage));

                    } else {
                        response.sendRedirect(redirectPage);
                    }
                    return;
                }
            } catch (DataException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //se la validazione fallisce...
        //if the validation fails...
        handleError("Login failed", request, response);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {

            Logger.getLogger(Login.class.getName()).log(Level.INFO, null, "Sono qui");            
            if (request.getParameter("login") != null) {
                action_login(request, response);
            } else {
                String https_redirect_url = SecurityHelpers.checkHttps(request);
                request.setAttribute("https-redirect", https_redirect_url);
                action_default(request, response);
            }
        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        }
    }
}
