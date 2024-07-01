package com.otus.highload.core.util;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.otus.highload.core.dao.User;
import com.otus.highload.core.repository.UserRepositoryMaster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("db_init_data")
@RequiredArgsConstructor
public class DbInitData {
  private final UserRepositoryMaster userRepositoryMaster;
  private final PasswordEncoder passwordEncoder;

  @Value("classpath:people.v2.csv")
  private Resource peoples;

  @PostConstruct
  protected void init() throws IOException {
    log.info("start loaded users");

    var strategy = new HeaderColumnNameMappingStrategy<CsvData>();
    strategy.setType(CsvData.class);

    var path = Paths.get(peoples.getURI());
    try (var reader = Files.newBufferedReader(path)) {
      var beanLoader =
          new CsvToBeanBuilder<CsvData>(reader)
              .withType(CsvData.class)
              .withSeparator(',')
              .withIgnoreLeadingWhiteSpace(true)
              .withMappingStrategy(strategy)
              .build();
      var password = passwordEncoder.encode("asd");

      var users = beanLoader.parse();
      users.parallelStream()
          .forEach(
              data -> {
                var name = data.name.split(" ");

                var user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setCreated(LocalDateTime.now());
                user.setFirstName(name[1]);
                user.setSecondName(name[0]);
                var transliterate = transliterate(data.name.replace(' ', '.').toLowerCase());
                var email = transliterate + "@mail.com";
                for (var i = 1; userRepositoryMaster.existByEmail(email); i++) {
                  email = transliterate + i + "@mail.com";
                }
                user.setEmail(email);
                user.setBirthdate(data.date);
                user.setCity(data.city);
                user.setPassword(password);

                if (name[0].charAt(name[0].length() - 1) == 'а'
                    || name[0].substring(Math.max(name[0].length() - 2, 0)).equals("ая")) {
                  user.setGender("Женский");
                } else {
                  user.setGender("Мужской");
                }
                userRepositoryMaster.save(user);
              });
      log.info("users loaded {}", users.size());
    }
  }

  public static String transliterate(String message) {
    char[] abcCyr = {
      'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с',
      'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', '.'
    };
    String[] abcLat = {
      "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r",
      "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "."
    };
    var builder = new StringBuilder();
    for (var i = 0; i < message.length(); i++) {
      for (var x = 0; x < abcCyr.length; x++) {
        if (message.charAt(i) == abcCyr[x]) {
          builder.append(abcLat[x]);
        }
      }
    }
    return builder.toString();
  }

  @Data
  public static class CsvData {
    @CsvBindByName(column = "name")
    private String name;

    @CsvDate(value = "yyyy-MM-dd")
    @CsvBindByName(column = "date")
    private LocalDate date;

    @CsvBindByName(column = "city")
    private String city;
  }
}
