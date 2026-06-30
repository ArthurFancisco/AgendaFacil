package br.com.agendafacilpro.web;

import org.slf4j.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    String expected(RuntimeException e, Model model) {
        model.addAttribute("title", "Algo precisa ser ajustado");
        model.addAttribute("message", humanMessage(e.getMessage()));
        return "error";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    String validation(Exception e, Model model) {
        model.addAttribute("title", "Verifique os dados");
        model.addAttribute("message", validationMessage(e));
        return "error";
    }

    @ExceptionHandler(Exception.class)
    String unexpected(Exception e, Model model) {
        log.error("Erro inesperado", e);
        model.addAttribute("title", "Não conseguimos concluir essa ação");
        model.addAttribute("message", "Tente novamente. Se continuar acontecendo, fale com o suporte técnico.");
        return "error";
    }

    private String validationMessage(Exception e) {
        if (e instanceof BindException bind && bind.hasErrors()) {
            String message = bind.getAllErrors().get(0).getDefaultMessage();
            return humanMessage(message);
        }
        if (e instanceof MethodArgumentNotValidException invalid && invalid.hasErrors()) {
            String message = invalid.getAllErrors().get(0).getDefaultMessage();
            return humanMessage(message);
        }
        return "Verifique os dados e tente novamente.";
    }

    private String humanMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Verifique os dados e tente novamente.";
        }
        if (message.contains("SQL") || message.contains("Exception") || message.contains("java.") || message.contains("org.")) {
            return "Verifique os dados e tente novamente.";
        }
        return message;
    }
}
