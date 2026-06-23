package br.com.agendafacilpro.service;

import java.time.LocalDate;
import java.time.LocalTime;

public record ManualAppointmentRequest(
        String customerName,
        String customerPhone,
        Long serviceId,
        Long professionalId,
        LocalDate date,
        LocalTime time,
        String internalNote,
        boolean forceBlockedCustomer
) {
}
