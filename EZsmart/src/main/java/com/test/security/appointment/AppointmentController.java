package com.test.security.appointment;

import com.test.security.user.Role;
import com.test.security.user.UserRepository;
import com.test.security.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.test.security.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    UserService userService;
    AppointmentRepository appointmentRepository;
    UserRepository userRepository;
    AppointmentService appointmentservice;

    @GetMapping("/{userId}")
    public List<Appointment> getUserAppointments(@AuthenticationPrincipal User user , @PathVariable Integer userId) {
        if(!user.getRole().equals(Role.ROLE_ADMIN) && !user.getId().equals(userId)) {
            System.out.println("User id: " + user.getId());
            System.out.println("Path id: " + userId);
            throw new RuntimeException("You are not authorized to access this resource");
        }
        User user1 = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentservice.getUserAppointments(user1);
    }

    @PostMapping
    public Appointment createAppointment(@AuthenticationPrincipal User user, @RequestBody AppointmentRequest request) {
        if(user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_SELLER)) {
            throw new RuntimeException("You are not authorized to create an appointment");
        }
        Appointment appointment = Appointment.builder()
                .user(user)
                .services(request.getServices())
                .createdAt(LocalDateTime.now())
                .status(AppointmentStatus.PENDING)
                .build();
        return appointmentservice.createAppointment(appointment);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Appointment updateAppointmentStatus(@PathVariable Long id, @RequestBody AppointmentStatusRequest request) {
        return appointmentservice.updateAppointmentStatus(id, request.getStatus());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Appointment> getAllAppointments() {
        return appointmentservice.getAllAppointments();
    }
}
