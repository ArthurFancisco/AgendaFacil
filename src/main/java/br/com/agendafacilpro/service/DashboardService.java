package br.com.agendafacilpro.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendafacilpro.domain.Appointment;
import br.com.agendafacilpro.domain.AppointmentStatus;
import br.com.agendafacilpro.repo.AppointmentRepo;

@Service
public class DashboardService {

    private final AppointmentRepo appointments;
    private final AppointmentService appointmentService;

    public DashboardService(AppointmentRepo a, AppointmentService s) {
        appointments = a;
        appointmentService = s;
    }

    public record Metric(String label, long value, String hint) {

    }

    public record Day(LocalDate date, String label, List<Appointment> appointments) {

    }

    public record Filter(LocalDate startDate, LocalDate endDate, Long professionalId, AppointmentStatus status) {

    }

    public record Data(List<Metric> metrics, List<Appointment> pending, List<Day> week, List<Appointment> today, List<Appointment> agenda, List<Appointment> history) {

    }

    @Transactional
    public Data data(Long est) {
        return data(est, new Filter(LocalDate.now(), LocalDate.now(), null, null));
    }

    @Transactional
    public Data data(Long est, Filter filter) {
        appointmentService.expire(est);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        LocalDateTime end7 = today.plusDays(7).atTime(LocalTime.MAX);
        LocalDate filterStart = filter.startDate() == null ? today : filter.startDate();
        LocalDate filterEnd = filter.endDate() == null ? filterStart : filter.endDate();
        if (filterEnd.isBefore(filterStart)) {
            filterEnd = filterStart;
        }
        List<Metric> metrics = List.of(
                new Metric("Agendamentos hoje", appointments.countByEstablishmentIdAndStartAtBetween(est, start, end), "Tudo que entrou para hoje"),
                new Metric("Pendentes", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.PENDING_APPROVAL, LocalDateTime.now().minusYears(3), end7), "Esperando aprovacao"),
                new Metric("Concluidos hoje", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.COMPLETED, start, end), "Atendimentos finalizados"),
                new Metric("Faltas hoje", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.NO_SHOW, start, end), "Clientes que nao vieram"),
                new Metric("Proximos 7 dias", appointments.countByEstablishmentIdAndStatusInAndStartAtBetween(est, List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_APPROVAL), start, end7), "Horarios ativos")
        );
        List<Appointment> agenda = appointments.findAgenda(
                est,
                filterStart.atStartOfDay(),
                filterEnd.plusDays(1).atStartOfDay(),
                filter.professionalId(),
                filter.status()
        );
        return new Data(
                metrics,
                appointments.findByEstablishmentIdAndStatusOrderByStartAtAsc(est, AppointmentStatus.PENDING_APPROVAL),
                week(est, today, filter.professionalId()),
                appointments.findByEstablishmentIdAndStartAtBetweenOrderByStartAtAsc(est, start, end),
                agenda,
                appointments.findTop20ByEstablishmentIdAndStatusInOrderByStartAtDesc(est, List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW, AppointmentStatus.COMPLETED, AppointmentStatus.EXPIRED))
        );
    }

    private List<Day> week(Long est, LocalDate today, Long professionalId) {
        List<Day> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            List<Appointment> dayAppointments;
            if (professionalId == null) {
                dayAppointments = appointments.findByEstablishmentIdAndStatusInAndStartAtBetweenOrderByStartAtAsc(est, List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_APPROVAL), day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            } else {
                dayAppointments = appointments.findAgenda(est, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), professionalId, null).stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.PENDING_APPROVAL)
                        .toList();
            }
            list.add(new Day(day, label(day.getDayOfWeek()), dayAppointments));
        }
        return list;
    }

    private String label(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "Seg";
            case TUESDAY -> "Ter";
            case WEDNESDAY -> "Qua";
            case THURSDAY -> "Qui";
            case FRIDAY -> "Sex";
            case SATURDAY -> "Sab";
            case SUNDAY -> "Dom";
        };
    }
}
