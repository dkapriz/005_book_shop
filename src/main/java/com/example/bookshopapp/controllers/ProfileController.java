package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.dto.ProfileUserDto;
import com.example.bookshopapp.api.response.TransactionalListResponse;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.TransactionalService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import static com.example.bookshopapp.service.TransactionalService.SORT_ASC;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(value = SCOPE_SESSION)
public class ProfileController {
    private final AuthService authService;
    private final TransactionalService transactionalService;

    @Autowired
    public ProfileController(AuthService authService, TransactionalService transactionalService) {
        this.authService = authService;
        this.transactionalService = transactionalService;
    }

    @GetMapping("/profile")
    public String handleProfile(Model model) throws ViewEmptyParameterException {
        handelModelProfile(model, false);
        return "profile";
    }

    @PostMapping("/profile/save")
    public String handleProfileSave(ProfileUserDto profileUserDto, Model model) throws ViewEmptyParameterException {
        if (authService.updateUserData(profileUserDto)) {
            return "redirect:/logout";
        }
        handelModelProfile(model, true);
        return "/profile";
    }

    @GetMapping("/profile/cancel")
    public String handleProfileCancel() throws NotFoundException {
        authService.deleteAddedUserContact();
        return "redirect:/profile";
    }

    private void handelModelProfile(Model model, boolean saved) throws ViewEmptyParameterException {
        TransactionalListResponse transactionalListResponse =
                transactionalService.getTransactionalList(SORT_ASC, 0, 5);
        model.addAttribute("curUser", authService.getProfileCurUser());
        model.addAttribute("transactionalList", transactionalListResponse.getTransactions());
        model.addAttribute("transactionalShowMore", transactionalListResponse.getCount() !=
                transactionalListResponse.getTransactions().size());
        model.addAttribute("isSaved", saved);
    }
}
