package com.reminder.model.repository;

import com.reminder.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User getUserByTelegram(String telegram);

    User getUserByName(String name);

}
