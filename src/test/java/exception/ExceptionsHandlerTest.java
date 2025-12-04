package exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.test_task.exception.ExceptionsHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionsHandlerTest {

    private ExceptionsHandler exceptionsHandler;

    @BeforeEach
    void setUp() {
        exceptionsHandler = new ExceptionsHandler();
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<ObjectError> fieldErrors = Arrays.asList(
                new FieldError("createHotelRequest", "name", "Name is required"),
                new FieldError("createHotelRequest", "address.city", "City is required")
        );
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        ResponseEntity<Map<String, String>> response = exceptionsHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsKeys("name", "address.city");
        assertThat(response.getBody().get("name")).isEqualTo("Name is required");
        assertThat(response.getBody().get("address.city")).isEqualTo("City is required");
    }

    @Test
    void handleRuntimeException_WithNestedCause_ShouldReturnMessageFromRootException() {
        RuntimeException cause = new RuntimeException("Root cause message");
        RuntimeException ex = new RuntimeException("Wrapper exception", cause);
        ResponseEntity<Map<String, String>> response = exceptionsHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("Wrapper exception");
    }
}