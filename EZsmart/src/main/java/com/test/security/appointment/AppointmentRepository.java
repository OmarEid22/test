package com.test.security.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import com.test.security.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUser(User user);
}
