package br.com.agendafacilpro.web;

import org.slf4j.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    String expected(RuntimeException e, Model model) {
        model.addAttribute("title", "Algo precisa ser ajustado");
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    String unexpected(Exception e, Model model) {
        log.error("Erro inesperado", e);
        model.addAttribute("title", "Não conseguimos concluir essa ação");
        model.addAttribute("message", "Tente novamente. Se continuar acontecendo, fale com o suporte técnico.");
        return "error";
    }
}
