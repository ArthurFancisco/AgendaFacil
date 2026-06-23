package br.com.agendafacilpro.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendafacilpro.domain.Appointment;
import br.com.agendafacilpro.domain.AppointmentStatus;
import br.com.agendafacilpro.domain.Customer;
import br.com.agendafacilpro.domain.Establishment;
import br.com.agendafacilpro.domain.Professional;
import br.com.agendafacilpro.domain.ServiceItem;
import br.com.agendafacilpro.repo.AppointmentRepo;
import br.com.agendafacilpro.repo.CustomerRepo;
import br.com.agendafacilpro.repo.ProfessionalRepo;
import br.com.agendafacilpro.repo.ServiceItemRepo;
import br.com.agendafacilpro.repo.TimeBlockRepo;
import br.com.agendafacilpro.ui.AppointmentViewUtil;

@Service
public class AppointmentService {

    private static final List<AppointmentStatus> BLOCKING = List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_APPROVAL);
    private final AppointmentRepo appointments;
    private final CustomerRepo customers;
    private final ServiceItemRepo services;
    private final ProfessionalRepo professionals;
    private final TimeBlockRepo blocks;
    private final BookingGuardService guard;
    private final AppointmentViewUtil view;

    public AppointmentService(AppointmentRepo a, CustomerRepo c, ServiceItemRepo s, ProfessionalRepo p, TimeBlockRepo b, BookingGuardService g, AppointmentViewUtil v) {
        appointments = a;
        customers = c;
        services = s;
        professionals = p;
        blocks = b;
        guard = g;
        view = v;
    }

    public record Slot(LocalTime start, LocalTime end, boolean available, String reason) {

    }

    public record Summary(String customer, String establishment, String service, String professional, LocalDateTime start, LocalDateTime end, String status, String whatsappUrl) {

    }

    @Transactional(readOnly = true)
    public List<Slot> slots(Long est, Long serviceId, Long professionalId, LocalDate date) {
        ServiceItem s = services.findByIdAndEstablishmentId(serviceId, est).orElseThrow();
        Professional p = professionals.findByIdAndEstablishmentId(professionalId, est).orElseThrow();
        List<Slot> out = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        while (!t.plusMinutes(s.getDurationMinutes()).isAfter(LocalTime.of(18, 0))) {
            LocalDateTime start = LocalDateTime.of(date, t), end = start.plusMinutes(s.getDurationMinutes());
            String r = reason(est, p.getId(), start, end);
            out.add(new Slot(t, end.toLocalTime(), r == null, r));
            t = t.plusMinutes(30);
        }
        return out;
    }

    @Transactional
    public Appointment create(Establishment est, Long serviceId, Long professionalId, LocalDate date, LocalTime time, String name, String phone, String ip, String honeypot) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Informe seu nome para o estabelecimento identificar sua reserva.");
        }
        expire(est.getId());
        BookingGuardService.Decision d = guard.check(est, phone, ip, honeypot);
        if (!d.allowed()) {
            throw new IllegalArgumentException(d.message());
        }
        ServiceItem s = services.findByIdAndEstablishmentId(serviceId, est.getId()).filter(ServiceItem::isActive).orElseThrow(() -> new IllegalArgumentException("Serviço indisponível."));
        Professional p = professionals.findByIdAndEstablishmentId(professionalId, est.getId()).filter(Professional::isActive).orElseThrow(() -> new IllegalArgumentException("Profissional indisponível."));
        LocalDateTime start = LocalDateTime.of(date, time), end = start.plusMinutes(s.getDurationMinutes());
        if (!start.isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Esse horário já passou. Escolha outro horário disponível.");
        }
        noConflict(est.getId(), p.getId(), start, end, null);
        Optional<Customer> old = customers.findByEstablishmentIdAndPhoneNormalized(est.getId(), d.normalizedPhone());
        boolean isNew = old.isEmpty();
        Customer c = old.orElseGet(Customer::new);
        c.setEstablishment(est);
        c.setName(name.trim());
        c.setPhoneNormalized(d.normalizedPhone());
        if (c.isBlocked()) {
            throw new IllegalArgumentException("Seu número está bloqueado para agendamento online. Fale com o estabelecimento pelo WhatsApp.");
        }
        c = customers.save(c);
        Appointment a = new Appointment();
        a.setEstablishment(est);
        a.setCustomer(c);
        a.setServiceItem(s);
        a.setProfessional(p);
        a.setStartAt(start);
        a.setEndAt(end);
        a.setClientIp(ip);
        a.setStatus((isNew || c.getNoShowCount() >= 2) ? AppointmentStatus.PENDING_APPROVAL : AppointmentStatus.CONFIRMED);
        return appointments.save(a);
    }

    @Transactional(readOnly = true)
    public Summary summary(Establishment est, Long id) {
        Appointment a = appointments.findByIdAndEstablishmentId(id, est.getId()).orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        String text = "Olá! Fiz uma solicitação de agendamento para " + a.getServiceItem().getName() + " com " + a.getProfessional().getName() + ".";
        String url = "https://wa.me/" + est.getWhatsapp() + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        return new Summary(a.getCustomer().getName(), est.getName(), a.getServiceItem().getName(), a.getProfessional().getName(), a.getStartAt(), a.getEndAt(), view.statusLabel(a.getStatus()), url);
    }

    @Transactional
    public void approve(Long id, Long est) {
        Appointment a = owned(id, est);
        if (a.getStatus() != AppointmentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Essa reserva não está pendente.");
        
        }noConflict(est, a.getProfessional().getId(), a.getStartAt(), a.getEndAt(), a.getId());
        a.setStatus(AppointmentStatus.CONFIRMED);
        a.setApprovedAt(LocalDateTime.now());
    }

    @Transactional
    public void reject(Long id, Long est) {
        Appointment a = owned(id, est);
        if (a.getStatus() != AppointmentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Somente reservas pendentes podem ser recusadas.");
        
        }a.setStatus(AppointmentStatus.CANCELLED);
        a.setCancellationReason("Recusado pelo estabelecimento");
    }

    @Transactional
    public void cancel(Long id, Long est, String reason) {
        Appointment a = owned(id, est);
        if (a.getStatus() != AppointmentStatus.CONFIRMED && a.getStatus() != AppointmentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Esse agendamento já está encerrado.");
        
        }a.setStatus(AppointmentStatus.CANCELLED);
        a.setCancellationReason(reason == null || reason.isBlank() ? "Cancelado pelo estabelecimento" : reason.trim());
    }

    @Transactional
    public void complete(Long id, Long est) {
        Appointment a = owned(id, est);
        if (a.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Somente confirmados podem ser concluídos.");
        
        }a.setStatus(AppointmentStatus.COMPLETED);
        a.setCompletedAt(LocalDateTime.now());
    }

    @Transactional
    public void noShow(Long id, Long est) {
        Appointment a = owned(id, est);
        if (a.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Somente confirmados podem receber falta.");
        
        }a.setStatus(AppointmentStatus.NO_SHOW);
        a.getCustomer().addNoShow();
    }

    @Transactional
    public void expire(Long est) {
        appointments.findByEstablishmentIdAndStatusAndStartAtBefore(est, AppointmentStatus.PENDING_APPROVAL, LocalDateTime.now()).forEach(a -> {
            a.setStatus(AppointmentStatus.EXPIRED);
            a.setCancellationReason("Reserva pendente expirou automaticamente");
        });
    }

    private Appointment owned(Long id, Long est) {
        return appointments.findByIdAndEstablishmentId(id, est).orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para este estabelecimento."));
    }

    private String reason(Long est, Long prof, LocalDateTime start, LocalDateTime end) {
        if (!start.isAfter(LocalDateTime.now().minusMinutes(1))) {
            return "Horário já passou";
        
        }if (blocks.existsOverlap(est, prof, start, end)) {
            return "Bloqueado pelo estabelecimento";
        
        }if (appointments.existsBlockingOverlap(est, prof, start, end, BLOCKING, null)) {
            return "Já existe reserva nesse horário";
        
        }return null;
    }

    private void noConflict(Long est, Long prof, LocalDateTime start, LocalDateTime end, Long ignore) {
        if (blocks.existsOverlap(est, prof, start, end)) {
            throw new IllegalArgumentException("Esse horário foi bloqueado pelo estabelecimento.");
        
        }if (appointments.existsBlockingOverlap(est, prof, start, end, BLOCKING, ignore)) {
            throw new IllegalArgumentException("Esse horário acabou de ser reservado. Escolha outro horário disponível.");
    
        }}
}
