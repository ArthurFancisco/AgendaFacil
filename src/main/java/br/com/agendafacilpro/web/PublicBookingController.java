package br.com.agendafacilpro.web;

import br.com.agendafacilpro.domain.*;
import br.com.agendafacilpro.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;

@Controller
public class PublicBookingController {

    private final CatalogService catalog;
    private final AppointmentService appointments;
    private final EstablishmentSettingsService settings;

    private static final List<FlowStep> FLOW_STEPS = List.of(
            new FlowStep(1, "Serviço"),
            new FlowStep(2, "Profissional"),
            new FlowStep(3, "Data"),
            new FlowStep(4, "Horário"),
            new FlowStep(5, "Dados"),
            new FlowStep(6, "Confirmação")
    );

    public record FlowStep(int number, String label) {
    }

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
        addFlow(model, 1);
        return "public/establishment";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais")
    String professionals(@PathVariable String slug, @PathVariable Long serviceId, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("establishment", e);
        model.addAttribute("service", catalog.service(serviceId, e.getId()));
        model.addAttribute("professionals", catalog.professionalsForService(e.getId(), serviceId));
        model.addAttribute("settings", settings.forEstablishment(e));
        addFlow(model, 2);
        return "public/professionals";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais/{professionalId}/horarios")
    String slots(@PathVariable String slug, @PathVariable Long serviceId, @PathVariable Long professionalId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        Establishment e = catalog.establishment(slug);
        LocalDate selected = date == null ? LocalDate.now() : date;
        List<AppointmentService.Slot> slots = appointments.slots(e.getId(), serviceId, professionalId, selected);
        model.addAttribute("establishment", e);
        model.addAttribute("service", catalog.service(serviceId, e.getId()));
        model.addAttribute("professional", catalog.professionalForService(professionalId, serviceId, e.getId()));
        model.addAttribute("selectedDate", selected);
        model.addAttribute("slots", slots);
        model.addAttribute("slotSuggestions", slotSuggestions(slots));
        model.addAttribute("settings", settings.forEstablishment(e));
        addFlow(model, 4);
        return "public/slots";
    }

    @GetMapping("/agenda/{slug}/servicos/{serviceId}/profissionais/{professionalId}/dados")
    String data(@PathVariable String slug, @PathVariable Long serviceId, @PathVariable Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, @RequestParam(required = false) String customerName, @RequestParam(required = false) String customerPhone, Model model) {
        Establishment e = catalog.establishment(slug);
        dataModel(model, e, serviceId, professionalId, date, time, customerName, customerPhone);
        return "public/data";
    }

    @PostMapping("/agenda/{slug}/revisar")
    String review(@PathVariable String slug, @RequestParam Long serviceId, @RequestParam Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, @RequestParam String customerName, @RequestParam String customerPhone, @RequestParam(required = false) String website, Model model) {
        Establishment e = catalog.establishment(slug);
        try {
            appointments.validatePublicSlot(e, serviceId, professionalId, date, time);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            dataModel(model, e, serviceId, professionalId, date, time, customerName, customerPhone);
            model.addAttribute("error", ex.getMessage());
            return "public/data";
        }
        dataModel(model, e, serviceId, professionalId, date, time, customerName, customerPhone);
        model.addAttribute("website", website);
        addFlow(model, 6);
        return "public/confirm";
    }

    @PostMapping("/agenda/{slug}/confirmar")
    String confirm(@PathVariable String slug, @RequestParam Long serviceId, @RequestParam Long professionalId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, @RequestParam String customerName, @RequestParam String customerPhone, @RequestParam(required = false) String website, HttpServletRequest request, Model model) {
        Establishment e = catalog.establishment(slug);
        try {
            Appointment a = appointments.create(e, serviceId, professionalId, date, time, customerName, customerPhone, request.getRemoteAddr(), website);
            return "redirect:/agenda/" + slug + "/sucesso/" + a.getPublicToken();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            dataModel(model, e, serviceId, professionalId, date, time, customerName, customerPhone);
            model.addAttribute("error", ex.getMessage());
            return "public/data";
        }
    }

    @GetMapping("/agenda/{slug}/sucesso/{publicToken:[A-Za-z0-9_-]{24,64}}")
    String success(@PathVariable String slug, @PathVariable String publicToken, Model model) {
        Establishment e = catalog.establishment(slug);
        model.addAttribute("summary", appointments.summary(e, publicToken));
        addFlow(model, 6);
        return "public/success";
    }

    private void dataModel(Model model, Establishment establishment, Long serviceId, Long professionalId, LocalDate date, LocalTime time, String customerName, String customerPhone) {
        model.addAttribute("establishment", establishment);
        model.addAttribute("service", catalog.service(serviceId, establishment.getId()));
        model.addAttribute("professional", catalog.professionalForService(professionalId, serviceId, establishment.getId()));
        model.addAttribute("settings", settings.forEstablishment(establishment));
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedTime", time);
        model.addAttribute("customerName", customerName);
        model.addAttribute("customerPhone", customerPhone);
        addFlow(model, 5);
    }

    private void addFlow(Model model, int currentStep) {
        model.addAttribute("flowSteps", FLOW_STEPS);
        model.addAttribute("currentStep", currentStep);
        model.addAttribute("currentStepLabel", FLOW_STEPS.stream()
                .filter(step -> step.number() == currentStep)
                .map(FlowStep::label)
                .findFirst()
                .orElse(""));
        model.addAttribute("flowProgress", currentStep * 100 / FLOW_STEPS.size());
    }

    private List<String> slotSuggestions(List<AppointmentService.Slot> slots) {
        return slots.stream()
                .map(AppointmentService.Slot::suggestionText)
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .limit(3)
                .toList();
    }
}
