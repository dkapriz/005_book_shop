package com.example.bookshopapp.service;

import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.Author;
import com.example.bookshopapp.repositories.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class AuthorService {
    protected static final String[] LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З",
            "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Э", "Ю",
            "Я"};
    protected static final String[] LETTERS_EN = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "a", "b", "v", "g", "d", "e", "e", "ge", "z",
            "i", "ik", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "c", "ch", "sh", "shh", "ye", "yu",
            "ya"};

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Map<String, List<Author>> getAuthorsMap() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream().collect(Collectors.groupingBy((Author a) -> a.getName().substring(0, 1)));
    }

    public Map<String, String> getLetterMapEn() {
        return IntStream.range(0, LETTERS.length).boxed()
                .collect(Collectors.toMap(i -> LETTERS[i], i -> LETTERS_EN[i]));
    }

    public Author getAuthorBySlug(String slug) throws ViewNotFoundParameterException {
        Optional<Author> author = authorRepository.getAuthorBySlug(slug);
        if (!author.isPresent()) {
            log.warn("getAuthorBySlug (handling null value) slug:" + slug);
            throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return author.get();
    }

    public List<String> getAuthorParseDescription(String description) {
        Document document = Jsoup.parse(description);
        Elements elements = document.select("p");
        return elements.stream().map(Element::text).collect(Collectors.toList());
    }

    public List<Author> getAuthorsByBookId(Integer bookId) {
        return authorRepository.getAuthorByBookId(bookId);
    }
}
