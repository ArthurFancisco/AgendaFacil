package br.com.agendafacilpro.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.agendafacilpro.domain.AppUser;
import br.com.agendafacilpro.domain.AppointmentStatus;
import br.com.agendafacilpro.domain.Customer;
import br.com.agendafacilpro.domain.Professional;
import br.com.agendafacilpro.domain.ServiceItem;
import br.com.agendafacilpro.domain.TimeBlock;
import br.com.agendafacilpro.repo.CustomerRepo;
import br.com.agendafacilpro.repo.ProfessionalRepo;
import br.com.agendafacilpro.repo.ServiceItemRepo;
import br.com.agendafacilpro.repo.TimeBlockRepo;
import br.com.agendafacilpro.service.AppointmentService;
import br.com.agendafacilpro.service.CurrentUserService;
import br.com.agendafacilpro.service.DashboardService;
import br.com.agendafacilpro.service.EstablishmentSettingsForm;
import br.com.agendafacilpro.service.EstablishmentSettingsService;
import br.com.agendafacilpro.service.ManualAppointmentRequest;
import br.com.agendafacilpro.util.PhoneNormalizer;

@Controller
public class PanelController {

    private final CurrentUserService current;
    private final DashboardService dashboard;
    private final AppointmentService appointments;
    private final ServiceItemRepo services;
    private final ProfessionalRepo professionals;
    private final TimeBlockRepo blocks;
    private final CustomerRepo customers;
    private final EstablishmentSettingsService settingsService;

    public PanelController(CurrentUserService c, DashboardService d, AppointmentService a, ServiceItemRepo s, ProfessionalRepo p, TimeBlockRepo b, CustomerRepo customers, EstablishmentSettingsService settingsService) {
        current = c;
        dashboard = d;
        appointments = a;
        services = s;
        professionals = p;
        blocks = b;
        this.customers = customers;
        this.settingsService = settingsService;
    }

    @GetMapping("/panel")
    String panel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                 @RequestParam(required = false) Long professionalId,
                 @RequestParam(required = false) AppointmentStatus status,
                 @RequestParam(required = false) String customerPhone,
                 Model model) {
        AppUser u = current.user();
        Long est = u.getEstablishment().getId();
        LocalDate selectedStart = startDate == null ? LocalDate.now() : startDate;
        DashboardService.Filter filter = new DashboardService.Filter(selectedStart, endDate == null ? selectedStart : endDate, professionalId, status);
        model.addAttribute("user", u);
        model.addAttribute("establishment", u.getEstablishment());
        model.addAttribute("dashboard", dashboard.data(est, filter));
        model.addAttribute("services", services.findByEstablishmentIdOrderByActiveDescSortOrderAscNameAsc(est));
        model.addAttribute("activeServices", services.findByEstablishmentIdAndActiveTrueOrderBySortOrderAscNameAsc(est));
        model.addAttribute("professionals", professionals.findByEstablishmentIdOrderByActiveDescSortOrderAscNameAsc(est));
        model.addAttribute("activeProfessionals", professionals.findByEstablishmentIdAndActiveTrueOrderBySortOrderAscNameAsc(est));
        model.addAttribute("timeBlocks", blocks.findTop20ByEstablishmentIdOrderByStartAtDesc(est));
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("filter", filter);
        model.addAttribute("todayDate", LocalDate.now());
        model.addAttribute("next7Date", LocalDate.now().plusDays(6));
        model.addAttribute("customerLookupPhone", customerPhone);
        model.addAttribute("customerLookup", lookupCustomer(est, customerPhone));
        model.addAttribute("settings", settingsService.forEstablishment(u.getEstablishment()));
        return "panel/dashboard";
    }

    @PostMapping("/panel/appointments/manual")
    String manual(@RequestParam String customerName,
                  @RequestParam String customerPhone,
                  @RequestParam Long serviceId,
                  @RequestParam Long professionalId,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                  @RequestParam(required = false) String internalNote,
                  @RequestParam(defaultValue = "false") boolean forceBlockedCustomer,
                  RedirectAttributes r) {
        AppUser user = current.user();
        appointments.createManual(user.getEstablishment(), user, new ManualAppointmentRequest(customerName, customerPhone, serviceId, professionalId, date, time, internalNote, forceBlockedCustomer));
        r.addFlashAttribute("success", "Novo agendamento confirmado. Esse horario ficou indisponivel na agenda publica.");
        return "redirect:/panel#agenda";
    }

    @PostMapping("/panel/appointments/{id}/approve")
    String approve(@PathVariable Long id, RedirectAttributes r) {
        appointments.approve(id, current.establishmentId(), current.user());
        r.addFlashAttribute("success", "Reserva aprovada e horario confirmado.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/reject")
    String reject(@PathVariable Long id, RedirectAttributes r) {
        appointments.reject(id, current.establishmentId(), current.user());
        r.addFlashAttribute("success", "Reserva recusada. O horario voltou a ficar disponivel.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/complete")
    String complete(@PathVariable Long id, RedirectAttributes r) {
        appointments.complete(id, current.establishmentId(), current.user());
        r.addFlashAttribute("success", "Atendimento marcado como concluido.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/no-show")
    String noShow(@PathVariable Long id, RedirectAttributes r) {
        appointments.noShow(id, current.establishmentId(), current.user());
        r.addFlashAttribute("success", "Falta registrada no historico do cliente.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/cancel")
    String cancel(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes r) {
        appointments.cancel(id, current.establishmentId(), reason, current.user());
        r.addFlashAttribute("success", "Agendamento cancelado sem apagar o historico.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/services")
    String service(@RequestParam String name, @RequestParam int durationMinutes, @RequestParam(required = false) BigDecimal price, @RequestParam(required = false) String description, RedirectAttributes r) {
        AppUser u = current.user();
        ServiceItem s = new ServiceItem();
        s.setEstablishment(u.getEstablishment());
        s.setName(name.trim());
        s.setDurationMinutes(Math.max(15, durationMinutes));
        s.setPrice(price);
        s.setDescription(description);
        services.save(s);
        r.addFlashAttribute("success", "Servico cadastrado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/services/{id}/toggle")
    String toggleService(@PathVariable Long id, RedirectAttributes r) {
        ServiceItem s = services.findByIdAndEstablishmentId(id, current.establishmentId()).orElseThrow();
        s.setActive(!s.isActive());
        services.save(s);
        r.addFlashAttribute("success", s.isActive() ? "Servico reativado." : "Servico pausado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/professionals")
    String professional(@RequestParam String name, @RequestParam(required = false) String bio, @RequestParam(required = false) String whatsapp, RedirectAttributes r) {
        AppUser u = current.user();
        Professional p = new Professional();
        p.setEstablishment(u.getEstablishment());
        p.setName(name.trim());
        p.setBio(bio);
        p.setWhatsapp(whatsapp);
        professionals.save(p);
        r.addFlashAttribute("success", "Profissional cadastrado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/professionals/{id}/toggle")
    String toggleProf(@PathVariable Long id, RedirectAttributes r) {
        Professional p = professionals.findByIdAndEstablishmentId(id, current.establishmentId()).orElseThrow();
        p.setActive(!p.isActive());
        professionals.save(p);
        r.addFlashAttribute("success", p.isActive() ? "Profissional reativado." : "Profissional pausado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/time-blocks")
    String block(@RequestParam String title, @RequestParam(required = false) Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt, @RequestParam(required = false) String reason, RedirectAttributes r) {
        AppUser u = current.user();
        TimeBlock b = new TimeBlock();
        b.setEstablishment(u.getEstablishment());
        b.setTitle(title.trim());
        b.setStartAt(startAt);
        b.setEndAt(endAt);
        b.setReason(reason);
        if (professionalId != null) {
            b.setProfessional(professionals.findByIdAndEstablishmentId(professionalId, u.getEstablishment().getId()).orElseThrow());
        }
        blocks.save(b);
        r.addFlashAttribute("success", "Bloqueio de horario criado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/time-blocks/{id}/toggle")
    String toggleBlock(@PathVariable Long id, RedirectAttributes r) {
        TimeBlock b = blocks.findByIdAndEstablishmentId(id, current.establishmentId()).orElseThrow();
        b.setActive(!b.isActive());
        blocks.save(b);
        r.addFlashAttribute("success", b.isActive() ? "Bloqueio reativado." : "Bloqueio pausado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/settings")
    String settings(@RequestParam(defaultValue = "false") boolean newClientRequiresApproval,
                    @RequestParam int pendingExpirationMinutes,
                    @RequestParam int maxFutureAppointmentsPerPhone,
                    @RequestParam int maxAttemptsPerPhoneHour,
                    @RequestParam int maxAttemptsPerIpHour,
                    @RequestParam int noShowCountForManualApproval,
                    @RequestParam int noShowCountForBlock,
                    @RequestParam int minHoursBeforeClientCancel,
                    @RequestParam int longServiceManualApprovalMinutes,
                    @RequestParam(defaultValue = "false") boolean showPricesOnPublicPage,
                    RedirectAttributes r) {
        settingsService.update(current.user().getEstablishment(), new EstablishmentSettingsForm(
                newClientRequiresApproval,
                pendingExpirationMinutes,
                maxFutureAppointmentsPerPhone,
                maxAttemptsPerPhoneHour,
                maxAttemptsPerIpHour,
                noShowCountForManualApproval,
                noShowCountForBlock,
                minHoursBeforeClientCancel,
                longServiceManualApprovalMinutes,
                showPricesOnPublicPage
        ));
        r.addFlashAttribute("success", "Configuracoes salvas para este estabelecimento.");
        return "redirect:/panel#configuracoes";
    }

    private Customer lookupCustomer(Long establishmentId, String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        try {
            String normalized = PhoneNormalizer.normalize(phone);
            return customers.findByEstablishmentIdAndPhoneNormalized(establishmentId, normalized).orElse(null);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
