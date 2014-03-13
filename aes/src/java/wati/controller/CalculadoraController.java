/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "calculadoraController")
@SessionScoped
public class CalculadoraController implements Serializable {
    
    private String numeroCigarrosDia;
    private String custoMasso;
    private String custoSemana;
    private String custoMes;
    private String custoAno;
    
    public CalculadoraController() {
        
    }
    
    public void calcular( ActionEvent actionEvent ) {
        
        int numeroCigarrosDiaInt = Integer.valueOf( numeroCigarrosDia );
        double custoMassoDbl = Double.valueOf( custoMasso.replace("R$ ", "").replace(",", ".") );
        double numeroMassosDia = numeroCigarrosDiaInt / 20.0;
		
        double custoDia = numeroMassosDia * custoMassoDbl;
        custoSemana = String.format("%.2f", 7.0*custoDia);
        custoMes = String.format("%.2f", 30.0*custoDia);
        custoAno = String.format("%.2f", 365.0*custoDia);
        
    }

    /**
     * @return the numeroCigarrosDia
     */
    public String getNumeroCigarrosDia() {
        return numeroCigarrosDia;
    }

    /**
     * @param numeroCigarrosDia the numeroCigarrosDia to set
     */
    public void setNumeroCigarrosDia(String numeroCigarrosDia) {
        this.numeroCigarrosDia = numeroCigarrosDia;
    }

    /**
     * @return the custoMasso
     */
    public String getCustoMasso() {
        return custoMasso;
    }

    /**
     * @param custoMasso the custoMasso to set
     */
    public void setCustoMasso(String custoMasso) {
        this.custoMasso = custoMasso;
    }

    /**
     * @return the custoSemana
     */
    public String getCustoSemana() {
        return custoSemana;
    }

    /**
     * @param custoSemana the custoSemana to set
     */
    public void setCustoSemana(String custoSemana) {
        this.custoSemana = custoSemana;
    }

    /**
     * @return the custoMes
     */
    public String getCustoMes() {
        return custoMes;
    }

    /**
     * @param custoMes the custoMes to set
     */
    public void setCustoMes(String custoMes) {
        this.custoMes = custoMes;
    }

    /**
     * @return the custoAno
     */
    public String getCustoAno() {
        return custoAno;
    }

    /**
     * @param custoAno the custoAno to set
     */
    public void setCustoAno(String custoAno) {
        this.custoAno = custoAno;
    }
    
    
}
