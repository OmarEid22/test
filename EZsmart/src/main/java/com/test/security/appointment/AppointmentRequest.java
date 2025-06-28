package com.test.security.appointment;


import java.util.List;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AppointmentRequest {
    private List<String> services;
}
