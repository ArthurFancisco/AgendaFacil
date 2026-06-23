package br.com.agendafacilpro.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.agendafacilpro.domain.AppUser;
import br.com.agendafacilpro.domain.Professional;
import br.com.agendafacilpro.domain.ServiceItem;
import br.com.agendafacilpro.domain.TimeBlock;
import br.com.agendafacilpro.repo.ProfessionalRepo;
import br.com.agendafacilpro.repo.ServiceItemRepo;
import br.com.agendafacilpro.repo.TimeBlockRepo;
import br.com.agendafacilpro.service.AppointmentService;
import br.com.agendafacilpro.service.CurrentUserService;
import br.com.agendafacilpro.service.DashboardService;

@Controller
public class PanelController {

    private final CurrentUserService current;
    private final DashboardService dashboard;
    private final AppointmentService appointments;
    private final ServiceItemRepo services;
    private final ProfessionalRepo professionals;
    private final TimeBlockRepo blocks;

    public PanelController(CurrentUserService c, DashboardService d, AppointmentService a, ServiceItemRepo s, ProfessionalRepo p, TimeBlockRepo b) {
        current = c;
        dashboard = d;
        appointments = a;
        services = s;
        professionals = p;
        blocks = b;
    }

    @GetMapping("/panel")
    String panel(Model model) {
        AppUser u = current.user();
        Long est = u.getEstablishment().getId();
        model.addAttribute("user", u);
        model.addAttribute("establishment", u.getEstablishment());
        model.addAttribute("dashboard", dashboard.data(est));
        model.addAttribute("services", services.findByEstablishmentIdOrderByActiveDescSortOrderAscNameAsc(est));
        model.addAttribute("professionals", professionals.findByEstablishmentIdOrderByActiveDescSortOrderAscNameAsc(est));
        model.addAttribute("timeBlocks", blocks.findTop20ByEstablishmentIdOrderByStartAtDesc(est));
        return "panel/dashboard";
    }

    @PostMapping("/panel/appointments/{id}/approve")
    String approve(@PathVariable Long id, RedirectAttributes r) {
        appointments.approve(id, current.establishmentId());
        r.addFlashAttribute("success", "Reserva aprovada e horário confirmado.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/reject")
    String reject(@PathVariable Long id, RedirectAttributes r) {
        appointments.reject(id, current.establishmentId());
        r.addFlashAttribute("success", "Reserva recusada. O horário voltou a ficar disponível.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/complete")
    String complete(@PathVariable Long id, RedirectAttributes r) {
        appointments.complete(id, current.establishmentId());
        r.addFlashAttribute("success", "Atendimento marcado como concluído.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/no-show")
    String noShow(@PathVariable Long id, RedirectAttributes r) {
        appointments.noShow(id, current.establishmentId());
        r.addFlashAttribute("success", "Falta registrada no histórico do cliente.");
        return "redirect:/panel";
    }

    @PostMapping("/panel/appointments/{id}/cancel")
    String cancel(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes r) {
        appointments.cancel(id, current.establishmentId(), reason);
        r.addFlashAttribute("success", "Agendamento cancelado sem apagar o histórico.");
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
        r.addFlashAttribute("success", "Serviço cadastrado.");
        return "redirect:/panel#configuracoes";
    }

    @PostMapping("/panel/services/{id}/toggle")
    String toggleService(@PathVariable Long id, RedirectAttributes r) {
        ServiceItem s = services.findByIdAndEstablishmentId(id, current.establishmentId()).orElseThrow();
        s.setActive(!s.isActive());
        services.save(s);
        r.addFlashAttribute("success", s.isActive() ? "Serviço reativado." : "Serviço pausado.");
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
        r.addFlashAttribute("success", "Bloqueio de horário criado.");
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
}
