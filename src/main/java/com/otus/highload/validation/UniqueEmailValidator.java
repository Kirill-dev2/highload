package com.otus.highload.validation;

import com.otus.highload.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

@Slf4j
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

  private final UserService userService;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (StringUtils.isNotBlank(email) && userService.existByEmail(email)) {
      context.unwrap(HibernateConstraintValidatorContext.class).addMessageParameter("email", email);
      return false;
    }

    return true;
  }
}
