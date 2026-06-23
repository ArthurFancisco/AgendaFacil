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

    public record Data(List<Metric> metrics, List<Appointment> pending, List<Day> week, List<Appointment> today, List<Appointment> history) {

    }

    @Transactional
    public Data data(Long est) {
        appointmentService.expire(est);
        LocalDate d = LocalDate.now();
        LocalDateTime start = d.atStartOfDay(), end = d.plusDays(1).atStartOfDay(), end7 = d.plusDays(7).atTime(LocalTime.MAX);
        List<Metric> m = List.of(
                new Metric("Agendamentos hoje", appointments.countByEstablishmentIdAndStartAtBetween(est, start, end), "Tudo que entrou para hoje"),
                new Metric("Pendentes", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.PENDING_APPROVAL, LocalDateTime.now().minusYears(3), end7), "Esperando aprovação"),
                new Metric("Concluídos hoje", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.COMPLETED, start, end), "Atendimentos finalizados"),
                new Metric("Faltas hoje", appointments.countByEstablishmentIdAndStatusAndStartAtBetween(est, AppointmentStatus.NO_SHOW, start, end), "Clientes que não vieram"),
                new Metric("Próximos 7 dias", appointments.countByEstablishmentIdAndStatusInAndStartAtBetween(est, List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_APPROVAL), start, end7), "Horários ativos")
        );
        return new Data(m, appointments.findByEstablishmentIdAndStatusOrderByStartAtAsc(est, AppointmentStatus.PENDING_APPROVAL), week(est, d), appointments.findByEstablishmentIdAndStartAtBetweenOrderByStartAtAsc(est, start, end), appointments.findTop20ByEstablishmentIdAndStatusInOrderByStartAtDesc(est, List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW, AppointmentStatus.COMPLETED, AppointmentStatus.EXPIRED)));
    }

    private List<Day> week(Long est, LocalDate today) {
        List<Day> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            list.add(new Day(day, label(day.getDayOfWeek()), appointments.findByEstablishmentIdAndStatusInAndStartAtBetweenOrderByStartAtAsc(est, List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_APPROVAL), day.atStartOfDay(), day.plusDays(1).atStartOfDay())));
        }
        return list;
    }

    private String label(DayOfWeek d) {
        return switch (d) {
            case MONDAY ->
                "Seg";
            case TUESDAY ->
                "Ter";
            case WEDNESDAY ->
                "Qua";
            case THURSDAY ->
                "Qui";
            case FRIDAY ->
                "Sex";
            case SATURDAY ->
                "Sáb";
            case SUNDAY ->
                "Dom";
        };
    }
}
