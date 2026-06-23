package br.com.agendafacilpro.domain;

import java.time.LocalDateTime;
import java.util.List;

public final class AppointmentRules {

    private static final List<AppointmentStatus> BLOCKING_STATUSES = List.of(
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.PENDING_APPROVAL
    );

    private AppointmentRules() {
    }

    /**
     * Somente reservas confirmadas ou pendentes ainda válidas seguram o horário.
     */
    public static boolean blocksSchedule(AppointmentStatus status) {
        return BLOCKING_STATUSES.contains(status);
    }

    public static List<AppointmentStatus> blockingStatuses() {
        return BLOCKING_STATUSES;
    }

    /**
     * Regra canônica de interseção de horários:
     * novoInicio < fimExistente e novoFim > inicioExistente.
     */
    public static boolean overlaps(LocalDateTime newStart, LocalDateTime newEnd, LocalDateTime existingStart, LocalDateTime existingEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}
