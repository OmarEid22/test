package com.test.security.appointment;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusRequest {
    private AppointmentStatus status;
}
