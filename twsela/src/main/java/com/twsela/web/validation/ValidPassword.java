package com.twsela.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for passwords.
 * Requirements: 6+ chars, at least one letter, at least one digit.
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "كلمة المرور يجب أن تكون 6 أحرف على الأقل وتحتوي على حرف ورقم";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
