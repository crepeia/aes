package aes.utility;

import aes.controller.ChallengeUserController;
import aes.controller.ContactController;
import aes.controller.MobileOptionsController;
import aes.model.Contact;
import aes.model.Medal;
import aes.model.MedalUser;
import aes.model.Title;
import aes.model.TitleUser;
import aes.model.User;
import aes.persistence.ContactDAO;
import aes.persistence.MedalUserDAO;
import aes.persistence.TitleUserDAO;
import aes.service.ChallengeUserFacadeREST;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class Scheduler {

    @Inject
    private ContactController contactController;
    @Inject
    private MobileOptionsController mobileOptionsController;
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ContactDAO contactDAO;
    private MedalUserDAO medalUserDAO;
    private TitleUserDAO titleUserDAO;
    private EmailHelper emailHelper;

    public Scheduler() {
        emailHelper = new EmailHelper();
        try {
            contactDAO = new ContactDAO();
            contactDAO.setEntityManager(em);
            medalUserDAO = new MedalUserDAO();
            titleUserDAO = new TitleUserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(second = "0", minute = "0", hour = "7", dayOfWeek = "*")
    public void sendEmails() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Morning Task Running");
        sendScheduledEmails();
        //contactController.sendTestEmail();
    }

    @Schedule(second = "0", minute = "0", hour = "15", dayOfWeek = "*")
    public void afternoonTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Afternoon task running");
        sendScheduledEmails();
        //contactController.sendTestEmail();

    }

    @Schedule(second = "0", minute = "0", hour = "19", dayOfWeek = "*")
    public void eveningTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Evening task running");
        sendScheduledEmails();
        //contactController.sendTestEmail();

    }
   
    @Schedule(second = "0", minute = "0", hour = "0", dayOfWeek = "1")
    public void weeklyRankigMedal() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Set weekly ranking medal");
        setWeeklyRankigMedal();
    }
    
    @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1")
    public void monthlyRankigMedal() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Set monthly ranking medal");
        setMonthlyRankigMedal();
    }

    @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1", month = "1")
    public void yearlyRankigMedal() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Set yearly ranking medal");
        setYearlyRankigMedal();
    }


    @Schedule(second = "0", minute = "5", hour = "1", dayOfWeek = "*")
    public void sendTips() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Send Mobile Tips task running");
        mobileOptionsController.sendMobileTips();
    }

    //@Schedule(second = "0", minute = "0", hour = "*", dayOfWeek = "*", persistent = false)
    //public void appNotificationTask() {
    //    Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - App Notification task running");
    //    mobileOptionsController.sendScheduledNotifications();

    //}
    
   public void sendScheduledEmails() {

        Date today = new Date();
        List<Contact> contacts = contactDAO.getScheduledEmailsToDate(em, today);
     
        for (Contact contact : contacts) {
            try {
                if (contact.getSubject().contains("tips_subj")) {
                    emailHelper.sendTipsEmail(contact, em);
                   contactDAO.scheduleTipsEmail(contact.getUser(), em);
                } else {
                    emailHelper.sendHTMLEmail(contact, em);
                    if (contact.getSubject().contains("annualscreening_subj")) {
                      contactDAO.scheduleAnnualScreeningEmail(contact.getUser(), em);
                    }
                }

            }catch (SQLException ex){
                Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "Envio de email não pôde ser registrado. ");
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }catch(MessagingException ex){
                Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "Envio de email não pôde ser realizado. ");
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
    
    
    public static class RankLists {
        List<ChallengeUserController.NicknameScore> weeklyResult;
        List<ChallengeUserController.NicknameScore> monthlyResult;
        List<ChallengeUserController.NicknameScore> yearlyResult;

        public RankLists() {
            weeklyResult = new LinkedList<>();
            monthlyResult = new LinkedList<>();
            yearlyResult = new LinkedList<>();
        }
    }
    
    public void setWeeklyRankigMedal() {
        LocalDateTime localDateToday = LocalDateTime.now();
        LocalDateTime oneWeekBefore = localDateToday.minusDays(7);
        LocalDateTime lastDayOfWeekBefore = localDateToday.minusDays(1);

        long numeroDeSemanas = numberWeeklyInYear(oneWeekBefore);
               
        String descriptionMedal = "Top 3 Semanal - Semana " + numeroDeSemanas + " de " + oneWeekBefore.getYear();
        String descriptionTitle = "Top 3 da semana " + numeroDeSemanas + " de " + oneWeekBefore.getYear();
        
        String sendDate = oneWeekBefore.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));

        try {
            RankLists rank = new RankLists();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(sendDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.inRanking = 1").getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, oneWeekBefore.toLocalDate(), lastDayOfWeekBefore.toLocalDate());
                rank.weeklyResult.add(new ChallengeUserController.NicknameScore(u.getNickname(), points, u.getSelected_title()));
            });
           

            Comparator<ChallengeUserController.NicknameScore> comparador = new Comparator<ChallengeUserController.NicknameScore>(){
                @Override
                public int compare(ChallengeUserController.NicknameScore s1, ChallengeUserController.NicknameScore s2) {
                    return s2.getScore().compareTo(s1.getScore());
                }
            };
            
            Collections.sort(rank.weeklyResult, comparador);
            List<ChallengeUserController.NicknameScore> weeklyResult = rank.weeklyResult;
            
            int position = 1;
            Long lastScore = weeklyResult.get(0).getScore();
            
            for(int i=0; i<weeklyResult.size(); i++) {
                if (lastScore > weeklyResult.get(i).getScore() || lastScore < weeklyResult.get(i).getScore()) {
                    position = position +1;
                    lastScore = weeklyResult.get(i).getScore();
                }
                weeklyResult.get(i).setPosition(position);
            }
            
            int i = 0;
            while (true) {
                if (i == weeklyResult.size() || weeklyResult.get(i).getPosition() > 3){
                    break;
                } 
                
                try {
                    User userResult = (User) em.createQuery("SELECT u FROM User u WHERE u.nickname=:nm").setParameter("nm", weeklyResult.get(i).getNickname()).getSingleResult();

                    if (userResult != null) {
                        MedalUser valueMedal = new MedalUser(userResult, new Medal(3L), descriptionMedal, LocalDate.now());
                        TitleUser titleMedal = new TitleUser(userResult, new Title(3L), descriptionTitle, LocalDate.now());
                        
                        List<MedalUser> meList = em.createQuery("SELECT me FROM MedalUser me WHERE me.user.id=:userId AND me.medal.id=:medalId AND me.description=:medalDescription")
                                .setParameter("userId", valueMedal.getUser().getId())
                                .setParameter("medalId", valueMedal.getMedal().getId())
                                .setParameter("medalDescription", valueMedal.getDescription())
                                .getResultList();
                        
                                if (meList.isEmpty()) {
                                    medalUserDAO.insertOrUpdate(valueMedal, em);
                                    titleUserDAO.insertOrUpdate(titleMedal, em);
                                } 
                    }   
                } catch (Exception e) {
                    Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
                }

                i++;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    public void setMonthlyRankigMedal() {
        String[] monthName = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        
        LocalDateTime localDateToday = LocalDateTime.now();
        LocalDateTime monthBefore = localDateToday.minusMonths(1);
        LocalDateTime lastDayOfMonthBefore = localDateToday.minusDays(1);

        String descriptionMedal = "Top 3 Mensal - " + monthName[monthBefore.getMonthValue()-1] + " de " + monthBefore.getYear();
        String descriptionTitle = "Top 3 de " + monthName[monthBefore.getMonthValue()-1] + " de " + monthBefore.getYear();

        String sendDate = monthBefore.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));

        try {
            RankLists rank = new RankLists();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate dateStart = sdf.parse(sendDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.inRanking = 1").getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, monthBefore.toLocalDate(), lastDayOfMonthBefore.toLocalDate());
                rank.monthlyResult.add(new ChallengeUserController.NicknameScore(u.getNickname(), points, u.getSelected_title()));
            });

            Comparator<ChallengeUserController.NicknameScore> comparador = new Comparator<ChallengeUserController.NicknameScore>(){
                @Override
                public int compare(ChallengeUserController.NicknameScore s1, ChallengeUserController.NicknameScore s2) {
                    return s2.getScore().compareTo(s1.getScore());
                }
            };
            
            Collections.sort(rank.monthlyResult, comparador);
            List<ChallengeUserController.NicknameScore> monthlyResult = rank.monthlyResult;
            
            int position = 1;
            Long lastScore = monthlyResult.get(0).getScore();
            
            for(int i=0; i<monthlyResult.size(); i++) {
                if (lastScore > monthlyResult.get(i).getScore() || lastScore < monthlyResult.get(i).getScore()) {
                    position = position +1;
                    lastScore = monthlyResult.get(i).getScore();
                }
                monthlyResult.get(i).setPosition(position);
            }
            
            int i = 0;
            while (true) {
                if (i == monthlyResult.size() || monthlyResult.get(i).getPosition() > 3){
                    break;
                } 
                
                try {
                    User userResult = (User) em.createQuery("SELECT u FROM User u WHERE u.nickname=:nm").setParameter("nm", monthlyResult.get(i).getNickname()).getSingleResult();

                    if (userResult != null) {
                        MedalUser valueMedal = new MedalUser(userResult, new Medal(4L), descriptionMedal, LocalDate.now());
                        TitleUser titleMedal = new TitleUser(userResult, new Title(4L), descriptionTitle, LocalDate.now());
                        
                        List<MedalUser> meList = em.createQuery("SELECT me FROM MedalUser me WHERE me.user.id=:userId AND me.medal.id=:medalId AND me.description=:medalDescription")
                                .setParameter("userId", valueMedal.getUser().getId())
                                .setParameter("medalId", valueMedal.getMedal().getId())
                                .setParameter("medalDescription", valueMedal.getDescription())
                                .getResultList();
                        
                                if (meList.isEmpty()) {
                                    medalUserDAO.insertOrUpdate(valueMedal, em);
                                    titleUserDAO.insertOrUpdate(titleMedal, em);
                                } 
                    }   
                } catch (Exception e) {
                    Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
                }

                i++;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    public void setYearlyRankigMedal() {
        LocalDateTime localDateToday = LocalDateTime.now();
        LocalDateTime yearBefore = localDateToday.minusYears(1);
        LocalDateTime lastDayInYearBefore = localDateToday.of(yearBefore.getYear(), 12, 31, 0, 0);
 
        String descriptionMedal = "Top 3 Anual - Ano " + yearBefore.getYear();
        String descriptionTitle = "Top 3 de " + yearBefore.getYear();
  
        String sendDate = yearBefore.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
  
        try {
            RankLists rank = new RankLists();
            
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            LocalDate dateStart = sdf.parse(sendDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.inRanking = 1").getResultList();

            users.forEach(u -> {
                long points = getPointsFromDate(u, yearBefore.toLocalDate(), lastDayInYearBefore.toLocalDate());
                rank.yearlyResult.add(new ChallengeUserController.NicknameScore(u.getNickname(), points, u.getSelected_title()));
            });

            Comparator<ChallengeUserController.NicknameScore> comparador = new Comparator<ChallengeUserController.NicknameScore>(){
                @Override
                public int compare(ChallengeUserController.NicknameScore s1, ChallengeUserController.NicknameScore s2) {
                    return s2.getScore().compareTo(s1.getScore());
                }
            };
            
            Collections.sort(rank.yearlyResult, comparador);
            List<ChallengeUserController.NicknameScore> yearlyResult = rank.yearlyResult;
            
            int position = 1;
            Long lastScore = yearlyResult.get(0).getScore();
            
            for(int i=0; i<yearlyResult.size(); i++) {
                if (lastScore > yearlyResult.get(i).getScore() || lastScore < yearlyResult.get(i).getScore()) {
                    position = position +1;
                    lastScore = yearlyResult.get(i).getScore();
                }
                yearlyResult.get(i).setPosition(position);
            }
            
            int i = 0;
            while (true) {
                if (i == yearlyResult.size() || yearlyResult.get(i).getPosition() > 3){
                    break;
                } 
                
                try {
                    User userResult = (User) em.createQuery("SELECT u FROM User u WHERE u.nickname=:nm").setParameter("nm", yearlyResult.get(i).getNickname()).getSingleResult();

                    if (userResult != null) {
                        MedalUser valueMedal = new MedalUser(userResult, new Medal(5L), descriptionMedal, LocalDate.now());
                        TitleUser titleMedal = new TitleUser(userResult, new Title(5L), descriptionTitle, LocalDate.now());
                        
                        List<MedalUser> meList = em.createQuery("SELECT me FROM MedalUser me WHERE me.user.id=:userId AND me.medal.id=:medalId AND me.description=:medalDescription")
                                .setParameter("userId", valueMedal.getUser().getId())
                                .setParameter("medalId", valueMedal.getMedal().getId())
                                .setParameter("medalDescription", valueMedal.getDescription())
                                .getResultList();
                        
                                if (meList.isEmpty()) {
                                    medalUserDAO.insertOrUpdate(valueMedal, em);
                                    titleUserDAO.insertOrUpdate(titleMedal, em);
                                } 
                    }   
                } catch (Exception e) {
                    Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
                }

                i++;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ChallengeUserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }

    
    public long numberWeeklyInYear(LocalDateTime dateValue) {
        LocalDateTime firstDayInYear = LocalDateTime.of(dateValue.getYear(), 1, 1, 0, 0);
        
        LocalDateTime firstMondayInYear = firstDayInYear;
        while(firstMondayInYear.getDayOfWeek() != DayOfWeek.MONDAY) {
            firstMondayInYear = firstMondayInYear.plusDays(1);
        }
       
        long diffMinute = ChronoUnit.MILLIS.between(firstMondayInYear, dateValue);
        long diffWeeks = diffMinute / (1000 * 60 * 60 * 24 * 7);
        return diffWeeks + 1;
    }
    
   
    protected long getPointsFromDate(User u, LocalDate firstDate, LocalDate secondDate) {
        Long score = (Long) this.em
                .createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.dateCompleted >= :firstDate AND date_completed <= :secondDate AND c.user.id=:userId")
                .setParameter("firstDate", firstDate)
                .setParameter("secondDate", secondDate)
                .setParameter("userId", u.getId()).getSingleResult();
        if (score == null) {
            score = Long.valueOf(0);
        }
        return score;
    }
 
    
}
