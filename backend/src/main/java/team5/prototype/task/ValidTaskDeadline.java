package team5.prototype.task;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTaskDeadlineValidator.class)
public @interface ValidTaskDeadline {
    String message() default "Task deadline must be at least now + sum of all workflow step durations";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
