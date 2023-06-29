package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.TagDto;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Tag;
import com.example.bookshopapp.repositories.BookRepository;
import com.example.bookshopapp.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final BookRepository bookRepository;

    @Autowired
    public TagService(TagRepository tagRepository, BookRepository bookRepository) {
        this.tagRepository = tagRepository;
        this.bookRepository = bookRepository;
    }

    @Scheduled(cron = BookShopConfig.BOOK_UPDATE_FREQUENCY)
    public void updateTagWeight() {
        double bookCount = getBookCount();
        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            double tagWeightsNoNormalize = tag.getBooks().size() / bookCount;
            tag.setWeight(tagWeightsNoNormalize);
        }
        double normalizedCoefficient = 1 / tags.stream()
                .map(Tag::getWeight).max(Comparator.naturalOrder()).orElse(1.0);
        for (Tag tag : tags) {
            double tagWeightsNormalize = tag.getWeight() * normalizedCoefficient;
            tag.setWeight(tagWeightsNormalize);
            tagRepository.save(tag);
        }
        log.info("Update tags weight");
    }

    public List<TagDto> getTags() {
        return tagRepository.findAll().stream().map(this::tagToTagDto).collect(Collectors.toList());
    }

    public Set<Tag> getTagsByBooks(Set<Book> books) {
        if (books.isEmpty()) {
            return new HashSet<>();
        }
        return tagRepository.getTagsByBooks(books);
    }

    public List<TagDto> getTagsByBook(Integer bookId) {
        return tagRepository.getTagsByBook(bookId).stream().map(this::tagToTagDto).collect(Collectors.toList());
    }

    public Tag getTagBySlug(String slug) throws ViewNotFoundParameterException {
        Optional<Tag> tagOpt = tagRepository.getTagBySlug(slug);
        if (!tagOpt.isPresent()) {
            log.warn("getTagBySlug (handling null value) slug:" + slug);
            throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return tagOpt.get();
    }

    private int getBookCount() {
        return (int) bookRepository.count();
    }

    private TagDto tagToTagDto(Tag tag) {
        return new TagDto(
                tag.getName(),
                tag.getSlug(),
                getStrTagWeight(tag.getWeight())
        );
    }

    private String getStrTagWeight(Double weight) {
        if (weight <= BookShopConfig.TAG_WEIGHT_0_MAX_VALUE) {
            return BookShopConfig.TAG_WEIGHT_0_CLASS_NAME;
        }
        if (weight <= BookShopConfig.TAG_WEIGHT_1_MAX_VALUE) {
            return BookShopConfig.TAG_WEIGHT_1_CLASS_NAME;
        }
        if (weight <= BookShopConfig.TAG_WEIGHT_2_MAX_VALUE) {
            return BookShopConfig.TAG_WEIGHT_2_CLASS_NAME;
        }
        if (weight <= BookShopConfig.TAG_WEIGHT_3_MAX_VALUE) {
            return BookShopConfig.TAG_WEIGHT_3_CLASS_NAME;
        }
        return BookShopConfig.TAG_WEIGHT_4_CLASS_NAME;
    }
}
