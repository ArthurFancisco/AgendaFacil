package br.com.agendafacilpro.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EstablishmentSettingsForm(
        boolean newClientRequiresApproval,
        @Min(value = 5, message = "A expiração precisa ter pelo menos 5 minutos.")
        @Max(value = 1440, message = "A expiração não pode passar de 24 horas.")
        int pendingExpirationMinutes,
        @Min(value = 1, message = "Informe pelo menos 1 agendamento futuro.")
        @Max(value = 20, message = "O limite de agendamentos futuros está muito alto.")
        int maxFutureAppointmentsPerPhone,
        @Min(value = 1, message = "Informe pelo menos 1 tentativa por telefone.")
        @Max(value = 50, message = "O limite por telefone está muito alto.")
        int maxAttemptsPerPhoneHour,
        @Min(value = 1, message = "Informe pelo menos 1 tentativa por acesso.")
        @Max(value = 200, message = "O limite por acesso está muito alto.")
        int maxAttemptsPerIpHour,
        @Min(value = 1, message = "Informe pelo menos 1 falta.")
        @Max(value = 20, message = "O limite de faltas está muito alto.")
        int noShowCountForManualApproval,
        @Min(value = 1, message = "Informe pelo menos 1 falta para bloqueio.")
        @Max(value = 20, message = "O limite de bloqueio está muito alto.")
        int noShowCountForBlock,
        @Min(value = 0, message = "Informe um prazo válido.")
        @Max(value = 168, message = "O prazo máximo é de 168 horas.")
        int minHoursBeforeClientCancel,
        @Min(value = 15, message = "Serviços longos precisam ter pelo menos 15 minutos.")
        @Max(value = 480, message = "O limite de serviço longo está muito alto.")
        int longServiceManualApprovalMinutes,
        boolean showPricesOnPublicPage
) {
}
