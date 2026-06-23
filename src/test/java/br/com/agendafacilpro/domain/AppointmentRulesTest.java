package br.com.agendafacilpro.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AppointmentRulesTest {

    @Test
    void confirmedBlocksSchedule() {
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.CONFIRMED)).isTrue();
    }

    @Test
    void pendingApprovalBeforeExpirationBlocksSchedule() {
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.PENDING_APPROVAL)).isTrue();
    }

    @Test
    void finishedStatusesDoNotBlockSchedule() {
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.CANCELLED)).isFalse();
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.NO_SHOW)).isFalse();
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.COMPLETED)).isFalse();
        assertThat(AppointmentRules.blocksSchedule(AppointmentStatus.EXPIRED)).isFalse();
    }

    @Test
    void exactSameTimeConflicts() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 23, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 23, 10, 0);

        assertThat(AppointmentRules.overlaps(start, end, start, end)).isTrue();
    }

    @Test
    void partiallyOverlappingTimeConflicts() {
        LocalDateTime newStart = LocalDateTime.of(2026, 6, 23, 9, 30);
        LocalDateTime newEnd = LocalDateTime.of(2026, 6, 23, 10, 30);
        LocalDateTime existingStart = LocalDateTime.of(2026, 6, 23, 9, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2026, 6, 23, 10, 0);

        assertThat(AppointmentRules.overlaps(newStart, newEnd, existingStart, existingEnd)).isTrue();
    }

    @Test
    void endingBeforeNextStartsDoesNotConflict() {
        LocalDateTime newStart = LocalDateTime.of(2026, 6, 23, 9, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 6, 23, 9, 30);
        LocalDateTime existingStart = LocalDateTime.of(2026, 6, 23, 10, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2026, 6, 23, 11, 0);

        assertThat(AppointmentRules.overlaps(newStart, newEnd, existingStart, existingEnd)).isFalse();
    }

    @Test
    void startingAfterPreviousEndsDoesNotConflict() {
        LocalDateTime newStart = LocalDateTime.of(2026, 6, 23, 10, 30);
        LocalDateTime newEnd = LocalDateTime.of(2026, 6, 23, 11, 0);
        LocalDateTime existingStart = LocalDateTime.of(2026, 6, 23, 9, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2026, 6, 23, 10, 0);

        assertThat(AppointmentRules.overlaps(newStart, newEnd, existingStart, existingEnd)).isFalse();
    }
}
