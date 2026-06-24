package br.com.agendafacilpro.web;

import br.com.agendafacilpro.domain.*;
import br.com.agendafacilpro.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@Controller
public class PublicBookingController {

    private final CatalogService catalog;
    private final AppointmentService appointments;
    private final EstablishmentSettingsService settings;

    public PublicBookingController(CatalogService c, AppointmentService a, EstablishmentSettingsService settings) {
        catalog = c;
        appointments = a;
        this.settings = settings;
    }

    @GetMapping("/agenda/{slug}")
    String establishment(@PathVariable String slug, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("establishment", e);
        model.addAttribute("services", catalog.services(e.getId()));
        model.addAttribute("settings", settings.forEstablishment(e));
        return "public/establishment";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais")
    String professionals(@PathVariable String slug, @PathVariable Long serviceId, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("establishment", e);
        model.addAttribute("service", catalog.service(serviceId, e.getId()));
        model.addAttribute("professionals", catalog.professionalsForService(e.getId(), serviceId));
        return "public/professionals";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais/{professionalId}/horarios")
    String slots(@PathVariable String slug, @PathVariable Long serviceId, @PathVariable Long professionalId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        Establishment e = catalog.establishment(slug);
        LocalDate selected = date == null ? LocalDate.now() : date;
        model.addAttribute("establishment", e);
        model.addAttribute("service", catalog.service(serviceId, e.getId()));
        model.addAttribute("professional", catalog.professionalForService(professionalId, serviceId, e.getId()));
        model.addAttribute("selectedDate", selected);
        model.addAttribute("slots", appointments.slots(e.getId(), serviceId, professionalId, selected));
        return "public/slots";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais/{professionalId}/dados")
    String data(@PathVariable String slug, @PathVariable Long serviceId, @PathVariable Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("establishment", e);
        model.addAttribute("service", catalog.service(serviceId, e.getId()));
        model.addAttribute("professional", catalog.professionalForService(professionalId, serviceId, e.getId()));
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedTime", time);
        return "public/data";
    }

    @PostMapping("/agenda/{slug}/confirmar")
    String confirm(@PathVariable String slug, @RequestParam Long serviceId, @RequestParam Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, @RequestParam String customerName, @RequestParam String customerPhone, @RequestParam(required = false) String website, HttpServletRequest request) {
        Establishment e = catalog.establishment(slug);
        Appointment a = appointments.create(e, serviceId, professionalId, date, time, customerName, customerPhone, request.getRemoteAddr(), website);
        return "redirect:/agenda/" + slug + "/sucesso/" + a.getPublicToken();
    }

    @GetMapping("/agenda/{slug}/sucesso/{publicToken:[A-Za-z0-9_-]{24,64}}")
    String success(@PathVariable String slug, @PathVariable String publicToken, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("summary", appointments.summary(e, publicToken));
        return "public/success";
    }
}
