/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import aes.controller.ChallengeUserController;
import java.util.LinkedList;
import java.util.List;


public class RankLists {

    //todo: fix public fields
    public List<ChallengeUserController.NicknameScore> weeklyResult;
    public List<ChallengeUserController.NicknameScore> monthlyResult;
    public List<ChallengeUserController.NicknameScore> yearlyResult;

    public RankLists() {
        weeklyResult = new LinkedList<>();
        monthlyResult = new LinkedList<>();
        yearlyResult = new LinkedList<>();
    }
  }
