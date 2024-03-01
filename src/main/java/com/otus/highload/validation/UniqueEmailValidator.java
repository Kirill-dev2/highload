package com.otus.highload.validation;

import com.otus.highload.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

@Slf4j
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

  private final UserRepository userRepository;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (StringUtils.isNotBlank(email) && userRepository.existByEmail(email)) {
      context
          .unwrap(HibernateConstraintValidatorContext.class)
          .addMessageParameter("email", email);
      return false;
    }

    return true;
  }
}
