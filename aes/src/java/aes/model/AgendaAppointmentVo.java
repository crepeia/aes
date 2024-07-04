/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.time.LocalDateTime;

/**
 *
 * @author Malder
 */
public class AgendaAppointmentVo {
    
    public Long id;
    public Long userId;
    public Long consultantId;
    public LocalDateTime appointmentDate;

    public AgendaAppointmentVo(Long id, LocalDateTime appointmentDate, Long userId, Long consultantId) {
        this.id = id;
        this.userId = userId;
        this.consultantId = consultantId;
        this.appointmentDate = appointmentDate;
    }

    @Override
    public String toString() {
        return "AgendaAppointmentVo{" + "id=" + id + ", userId=" + userId + ", consultantId=" + consultantId + ", appointmentDate=" + appointmentDate + '}';
    }
    
    
}
