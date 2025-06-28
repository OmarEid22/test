package com.test.security.appointment;

import com.test.security.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import com.test.security.user.User;

@AllArgsConstructor
@Service
public class AppointmentService {

    AppointmentRepository appointmentRepository;
    UserService userService;

    public List<Appointment> getUserAppointments(User user) {
        return appointmentRepository.findByUser(user);
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

}
