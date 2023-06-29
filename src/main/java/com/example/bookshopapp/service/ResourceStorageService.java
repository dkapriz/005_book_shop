package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.BookFileDto;
import com.example.bookshopapp.api.dto.BookFileListDto;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.*;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.example.bookshopapp.repositories.BookFileRepository;
import com.example.bookshopapp.repositories.FileDownloadRepository;
import liquibase.util.file.FilenameUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_RESOURCE_NOT_FOUND;

@Service
@Slf4j
public class ResourceStorageService {
    private final BookFileRepository bookFileRepository;
    private final FileDownloadRepository fileDownloadRepository;
    private final Book2UserRepository book2UserRepository;
    private final AuthService authService;
    private final BookShopConfig config;

    @Autowired
    public ResourceStorageService(BookFileRepository bookFileRepository, FileDownloadRepository fileDownloadRepository,
                                  Book2UserRepository book2UserRepository, AuthService authService,
                                  BookShopConfig config) {
        this.bookFileRepository = bookFileRepository;
        this.fileDownloadRepository = fileDownloadRepository;
        this.book2UserRepository = book2UserRepository;
        this.authService = authService;
        this.config = config;
    }

    public String saveNewBookImage(MultipartFile file, String slug) throws IOException {
        String resourceURI = null;

        if (!file.isEmpty()) {
            if (!new File(config.getUploadPath()).exists()) {
                Files.createDirectories(Paths.get(config.getUploadPath()));
                log.info("create image folder in " + config.getUploadPath());
            }
            String fileName = slug + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path path = Paths.get(config.getUploadPath(), fileName);
            resourceURI = "/book-covers/" + fileName;
            file.transferTo(path);
            log.info(fileName + " uploaded OK!");
        }
        return resourceURI;
    }

    public String getFileSizeStr(Path path) throws IOException {
        double value = Files.readAllBytes(path).length * BookShopConfig.RATIO_BYTES_TO_MB;
        return String.format("%.1f", value) + " " + BookShopConfig.RATIO_BYTES_TO_MB_TEXT;
    }

    public Path getBookFilePath(String hash) throws BookListWrongParameterException {
        Optional<BookFile> bookFile = bookFileRepository.findBookFileByHash(hash);
        if (!bookFile.isPresent()) {
            log.warn("getBookFilePath (handling null value) hash:" + hash);
            throw new BookListWrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return Paths.get(bookFile.get().getPath());
    }

    public MediaType getBookFileMime(String hash) throws BookListWrongParameterException {
        Optional<BookFile> bookFile = bookFileRepository.findBookFileByHash(hash);
        if (!bookFile.isPresent()) {
            log.warn("getBookFileMime (handling null value) hash:" + hash);
            throw new BookListWrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        String mimeType = URLConnection.guessContentTypeFromName(Paths
                .get(bookFile.get().getPath()).getFileName().toString());
        if (mimeType != null) {
            return MediaType.parseMediaType(mimeType);
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public byte[] getBookFileByteArray(String hash) throws IOException, BookListWrongParameterException {
        Optional<BookFile> bookFile = bookFileRepository.findBookFileByHash(hash);
        if (!bookFile.isPresent()) {
            log.warn("getBookFileByteArray (handling null value) hash:" + hash);
            throw new BookListWrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        Path path = Paths.get(config.getDownloadPath(), bookFile.get().getPath());
        return Files.readAllBytes(path);
    }

    public BookFileListDto getBooksFiles(Book book) throws IOException {
        Set<BookFileDto> result = new TreeSet<>();
        for (BookFile bookFile : book.getBookFiles()) {
            BookFileDto bookFileDto = new BookFileDto(
                    bookFile.getBookFileType().getName(),
                    bookFile.getBookFileType().getDescription(),
                    getFileSizeStr(Paths.get(config.getDownloadPath(), bookFile.getPath())),
                    bookFile.getHash()
            );
            result.add(bookFileDto);
        }
        return new BookFileListDto(true, result);
    }

    public Book getBookByFileHash(String hash) throws ViewNotFoundParameterException {
        Optional<BookFile> bookFileOpt = bookFileRepository.findBookFileByHash(hash);
        if(!bookFileOpt.isPresent()){
            throw new ViewNotFoundParameterException(EX_MSG_RESOURCE_NOT_FOUND);
        }
        return bookFileOpt.get().getBook();
    }

    /**
     * Метод проверяет возможность скачивания файла пользователем. Если файл доступен, то увеличивается счетчик
     * скачиваний.
     * Производятся следующие проверки:
     * - авторизация пользователя
     * - книга находится в статусе PAID или ARCHIVE
     * - количество скачиваний
     * @return false - доступ запрещен, true - доступ разрешен.
     */
    @Transactional
    public boolean isAvailableFileAndUpdateCountDownload(String hash) {
        User user = authService.getCurrentUser();
        if(user == null){
            return false;
        }
        Book book;
        try {
            book = getBookByFileHash(hash);
        } catch (ViewNotFoundParameterException e) {
            return false;
        }
        if(!isBookPaid(book, user)){
            return false;
        }
        return isAvailableFile(book, user, true);
    }

    private void createFileDownloadRecord(Book book, User user){
        FileDownload fileDownload = new FileDownload();
        fileDownload.setBook(book);
        fileDownload.setUser(user);
        fileDownload.setCount(1);
        fileDownloadRepository.save(fileDownload);
    }

    private void incCountDownload(FileDownload fileDownload){
        fileDownload.setCount(fileDownload.getCount() + 1);
        fileDownloadRepository.save(fileDownload);
    }

    public byte[] getFileByHash(String hash) throws IOException {
        byte[] data = getBookFileByteArray(hash);
        log.info("book file data len: " + data.length);
        return data;
    }

    public Path getFilePathByHash(String hash){
        Path path = getBookFilePath(hash);
        log.info("book file path: " + path);
        return path;
    }

    public MediaType getFileMediaTypeByHash(String hash){
        MediaType mediaType = getBookFileMime(hash);
        log.info("book file mime type: " + mediaType);
        return mediaType;
    }

    public boolean isBookPaid(Book book, User user){
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByUserAndBook(user, book);
        return book2User.filter(value -> value.getBook2UserType().getCode().equals(BookStatus.PAID.getStatus()) ||
                value.getBook2UserType().getCode().equals(BookStatus.ARCHIVED.getStatus())).isPresent();
    }

    public boolean isAvailableFile(Book book, User user, boolean isIncrementCounter){
        Optional<FileDownload> fileDownload = fileDownloadRepository.getByBookAndUser(book, user);
        if(!fileDownload.isPresent()){
            createFileDownloadRecord(book, user);
            return true;
        }
        if(fileDownload.get().getCount() >= config.getMaxDownloadCount()){
            return false;
        }
        if(isIncrementCounter){
            incCountDownload(fileDownload.get());
        }
        return true;
    }
}