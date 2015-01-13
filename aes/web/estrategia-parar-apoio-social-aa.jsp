<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:p="http://primefaces.org/ui"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:w="http://java.sun.com/jsf/composite/ezcomp"
      xmlns:c="http://java.sun.com/jsp/jstl/core">
    <h:head>
        <title>Introdu��o - O quanto voce bebe ? | �lcool e Saude </title>
        <w:head_comum />
    </h:head>

    <h:body>
        <div id="corpo"> 
            <!-- header -->    
            <w:header />
            <w:topmenu />
            <!--mainmenu-->         	
            <div class="main">

                <div class="pagecontent">


                    <div id="pagetop">
                        <div id="hpagetop">
                            <h1>Planejando sua mudan�a</h1> 
                        </div>    

                        <div id="progressbar">
                            <p:progressBar value="50" labelTemplate="{value}%" displayOnly="true"/>
                        </div>                        
                    </div>


                    <div id="pagemiddle">
                        
                        <div class="fotoframe">                            
                            <h:graphicImage library="default" id="cartoonman" title="Parar de fumar" name="images/deixar-cigarro.png" alt="Gratuito" />
                        </div>

                        <div id="pmtext">

                            <h3>Parando de Beber - Grupos de Auto-Ajuda</h3>
                            <h4>Considere participar de um grupo de auto-ajuda como os �lcoolicos An�nimos</h4>
                            <li>Pessoas em recupera��o que frequentam grupos como o AA regurlamente tendem a terem melhores resultados dos que n�o frequentam </li>
                            <h4>Grupos podem variar bastante </h4>
                            <li>Procure um grupoem que voc� se sinta mais confort�vel</li> 
                            <li>Em um grupo que voc� se sinta mais confort�vel voc� ter� mais chances de se envolver, inclusive podendo ajudar outras pessoas no futuro</li>
                           
                            <h:form>

                                <div id="btnback">
                                    <h:button class="btngen" value ="#{messages['back']}" outcome="./quanto-voce-bebe-introducao.xhtml"></h:button>
                                </div>
                                
                                
                                <div id="btnnext">
                                    <p:commandButton id="saveBtn" styleClass="btngen" value="#{messages['next']}" update="@form" action="#{evaluationController.audit3()}"/>      
                                </div>

                            </h:form>

                        </div><!-- end pmtext -->  

                    </div><!-- end page middle -->                                             

                    <div class="referencias">
                        <p>Atualizado em 12/01/2014</p>
                    </div>



                </div>        
            </div>


        </div><!-- end.corpo --> 

        <!--Footer -->
        <w:footer />
    </h:body>
</html>

