/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.ChallengeUser;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import org.hibernate.TransientObjectException;
/**
 *
 * @author bruno
 */
@Named("challengeUserController")
@RequestScoped
public class ChallengeUserController extends BaseController<ChallengeUser> {
    
    public static class NicknameScore {
        private String nickname;
        private Long score;
        private int position;
        private Long title;
        public NicknameScore(String nickname, Long score, Long title){
            this.nickname = nickname;
            this.score = score;
            this.position = 0;
            this.title = title;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Long getScore() {
            return score;
        }

        public void setScore(Long score) {
            this.score = score;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Long getTitle() {
            return title;
        }

        public void setTitle(Long title) {
            this.title = title;
        }
        
    }
    
    List<NicknameScore> filteredResultList = new LinkedList<>();
    
    private List<ChallengeUser> userChallenges;
    private int rankFilter;
    private long selectedScore;
    private String selectedDate;
    
    private long userScoreWeekly;    
    private long userScoreMonthly;
    private long userScore;

   
    @Inject
    private UserController userController;
    
    
    @PostConstruct
    public void init() {
        try {
            LocalDate locDate = LocalDate.now();
            userScoreWeekly = getPointsFromDate(getUser(), locDate.with(ChronoField.DAY_OF_WEEK, 1));
            userScoreMonthly = getPointsFromDate(getUser(), locDate.with(ChronoField.DAY_OF_MONTH, 1));
            userScore = getPointsAllTime(getUser());
            
            
            filteredResultList = getRankFromDate(locDate.with(ChronoField.DAY_OF_WEEK, 1));
            rankFilter = 1;
            selectedDate = locDate.with(ChronoField.DAY_OF_WEEK, 1).toString();
            selectedScore = userScoreWeekly;
            
            Collections.sort(filteredResultList, (a, b) -> a.score > b.score? -1 : Objects.equals(a.score, b.score) ? 0 : 1);

            int  position = 1;
            Long lastScore = filteredResultList.get(0).getScore();

            for(NicknameScore fr: filteredResultList){
                if(!Objects.equals(fr.score, lastScore)){
                    position += 1;
                    lastScore = fr.score;
                }
                fr.setPosition(position);
            }
            
            daoBase = new GenericDAO<ChallengeUser>(ChallengeUser.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public User getUser() {
        return userController.getUser();
    }
    
    public List<NicknameScore> getRankFromDate(LocalDate dateStart){
        try {
            List<NicknameScore> resultList = new LinkedList<>();

            List<User> users = this.getEntityManager()
                    .createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                    .getResultList();

            for (User u : users) {
                long points = getPointsFromDate(u, dateStart);
                //if(points == null) points = Long.valueOf(0);
                resultList.add(new NicknameScore(u.getNickname(), points, u.getSelected_title()));
            }

            return resultList;

        } catch (TransientObjectException ex) {
            Logger.getLogger(ChallengeUserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public List<NicknameScore> getRankAllTime(){

            try {
                List<NicknameScore> resultList = new LinkedList<>();
                
                List<User> users = this.getEntityManager()
                        .createQuery("SELECT u FROM User u WHERE u.inRanking = 1")
                        .getResultList();
                        
                for (User u : users) {
                    long points = getPointsAllTime(u);
                    //if(points == null) points = Long.valueOf(0);
                    resultList.add(new NicknameScore(u.getNickname(), points, u.getSelected_title()));
                }
                
                return resultList;
                
            } catch (TransientObjectException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        return null;
    }
    
    
    
    public long getPointsFromDate(User u, LocalDate date){
        Long score = (Long) this.getEntityManager()
                        .createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.dateCompleted > :date AND c.user.id=:userId")
                        .setParameter("date", date)
                        .setParameter("userId", u.getId()).getSingleResult();
        if(score == null) score = Long.valueOf(0);
        return score;
    }
    
    public long getPointsAllTime(User u){
        Long score = (Long) this.getEntityManager()
                        .createQuery("SELECT SUM(c.score) FROM ChallengeUser c WHERE c.user.id=:userId")
                        .setParameter("userId", u.getId()).getSingleResult();
        if(score == null) score = Long.valueOf(0);
        return score;
    }
    
    public void listener(AjaxBehaviorEvent event) {
        System.out.println("listener");
        System.out.println("called by " + event.getComponent().getClass().getName());
        LocalDate dateStart = LocalDate.now();
        
        if(rankFilter == 1){
            selectedDate = dateStart.with(ChronoField.DAY_OF_WEEK, 1).toString();
            filteredResultList = getRankFromDate(dateStart.with(ChronoField.DAY_OF_WEEK, 1));
            
            selectedScore = userScoreWeekly;

        } else if(rankFilter == 2){
            selectedDate = dateStart.with(ChronoField.DAY_OF_MONTH, 1).toString();
            filteredResultList = getRankFromDate(dateStart.with(ChronoField.DAY_OF_MONTH, 1));
            selectedScore = userScoreMonthly;

        } else if(rankFilter == 3 ){
            selectedDate = dateStart.with(ChronoField.DAY_OF_YEAR, 1).toString();
            filteredResultList = getRankFromDate(dateStart.with(ChronoField.DAY_OF_YEAR, 1));
            selectedScore = userScore;
        } else if(rankFilter == 4 ){
            selectedDate = "TOTAL";
            filteredResultList = getRankAllTime();
            
            selectedScore = userScore;
        }else {
            filteredResultList.clear();
            selectedScore = 0;
        }
        Collections.sort(filteredResultList, (a, b) -> a.score > b.score? -1 : Objects.equals(a.score, b.score) ? 0 : 1);

        int  position = 1;
        Long lastScore = filteredResultList.get(0).getScore();
        
        for(NicknameScore fr: filteredResultList){
            if(!Objects.equals(fr.score, lastScore)){
                position += 1;
                lastScore = fr.score;
            }
            fr.setPosition(position);
        }
    }

    public List<ChallengeUser> getUserChallenges() {
        return userChallenges;
    }

    public void setUserChallenges(List<ChallengeUser> userChallenges) {
        this.userChallenges = userChallenges;
    }

    public int getRankFilter() {
        return rankFilter;
    }

    public void setRankFilter(int rankFilter) {
        this.rankFilter = rankFilter;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public List<NicknameScore> getFilteredResultList() {
        return filteredResultList;
    }

    public void setFilteredResultList(List<NicknameScore> resultList) {
        this.filteredResultList = resultList;
    }

    public long getUserScore() {
        return userScore;
    }

    public void setUserScore(long userScore) {
        this.userScore = userScore;
    }

    public long getUserScoreWeekly() {
        return userScoreWeekly;
    }

    public void setUserScoreWeekly(long userScoreWeekly) {
        this.userScoreWeekly = userScoreWeekly;
    }

    public long getUserScoreMonthly() {
        return userScoreMonthly;
    }

    public void setUserScoreMonthly(long userScoreMonthly) {
        this.userScoreMonthly = userScoreMonthly;
    }

    public long getSelectedScore() {
        return selectedScore;
    }

    public void setSelectedScore(long selectedScore) {
        this.selectedScore = selectedScore;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
    
    
    
    
}
