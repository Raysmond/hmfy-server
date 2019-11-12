package com.shield.service.event;

import com.shield.domain.Appointment;
import com.shield.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

//@Service
@Slf4j
public class AppointmentEntityListener {
//    @Autowired
//    private AppointmentRepository appointmentRepository;

    @PreUpdate
    @PrePersist
//    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeInsertOrUpdate(final Appointment appointment) {
        log.info("trigger AppointmentEntityListener.beforeInsertOrUpdate... Appointment: {}", appointment.toString());

//        if (appointment.getId() != null) {
//            Appointment before = appointmentRepository.getOne(appointment.getId());
//            log.info("before: {}", before.toString());
//        }
    }
}
