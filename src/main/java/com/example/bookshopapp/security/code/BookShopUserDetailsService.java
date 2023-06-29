package com.example.bookshopapp.security.code;

import com.example.bookshopapp.model.UserContact;
import com.example.bookshopapp.repositories.UserContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_USER_NOT_FOUND;

@Service
public class BookShopUserDetailsService implements UserDetailsService {

    private final UserContactRepository userContactRepository;

    @Autowired
    public BookShopUserDetailsService(UserContactRepository userContactRepository) {
        this.userContactRepository = userContactRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<UserContact> userContact = userContactRepository.findByContact(s);
        if (userContact.isPresent()) {
            return new BookShopUserDetails(userContact.get());
        } else {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
    }
}
