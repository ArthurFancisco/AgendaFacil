package br.com.agendafacilpro.ui;
import br.com.agendafacilpro.domain.AppointmentStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class AppointmentViewUtilTest {
  private final AppointmentViewUtil view = new AppointmentViewUtil();
  @Test void translatesStatus() {
    assertThat(view.statusLabel(AppointmentStatus.PENDING_APPROVAL)).isEqualTo("Pendente");
    assertThat(view.statusLabel(AppointmentStatus.CONFIRMED)).isEqualTo("Confirmado");
    assertThat(view.statusLabel(AppointmentStatus.NO_SHOW)).isEqualTo("Faltou");
  }
  @Test void actionRulesAreSafe() {
    assertThat(view.canApprove(AppointmentStatus.PENDING_APPROVAL)).isTrue();
    assertThat(view.canCancel(AppointmentStatus.CONFIRMED)).isTrue();
    assertThat(view.canComplete(AppointmentStatus.CANCELLED)).isFalse();
  }
}
