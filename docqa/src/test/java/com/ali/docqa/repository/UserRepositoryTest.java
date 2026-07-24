package com.ali.docqa.repository;

import org.junit.jupiter.api.Test;
import com.ali.docqa.model.User;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;  
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.beans.factory.annotation.Autowired;


@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setUsername("testuser");
        user.setPasswordhash("password");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@gmail.com");
        assertThat(foundUser).isPresent();
    }
}
