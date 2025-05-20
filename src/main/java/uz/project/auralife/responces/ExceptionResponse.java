package uz.project.auralife.responces;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse extends Response {
    private String action;
    private String message;
    private String reason;
    private String stackTrace;
    private String className;

}
