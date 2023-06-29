package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    Page<Book> findAllByPubDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Book> findAllByPubDateBefore(LocalDate date, Pageable pageable);

    Page<Book> findAllByPubDateAfter(LocalDate date, Pageable pageable);

    Page<Book> findAllByTitleContainingIgnoreCase(String bookTitle, Pageable pageable);

    Optional<Book> findBookBySlug(String bookSlug);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.tags t WHERE t.slug = :tagSlugName")
    Page<Book> findAllByTagSlugName(Pageable pageable, @Param("tagSlugName") String tagSlugName);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.tags t WHERE t.id = :id")
    Page<Book> findAllByTagId(Pageable pageable, @Param("id") Integer id);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.genres g WHERE g.slug = :genreSlugName")
    Page<Book> findAllByGenreSlugName(Pageable pageable, @Param("genreSlugName") String genreSlugName);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.genres g WHERE g.id = :id")
    Page<Book> findAllByGenreId(Pageable pageable, @Param("id") Integer id);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.authors a WHERE a.author.slug = :authorSlugName")
    Page<Book> findAllByAuthorSlugName(Pageable pageable, @Param("authorSlugName") String genreSlugName);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.authors a WHERE a.author.id = :id")
    Page<Book> findAllByAuthorId(Pageable pageable, @Param("id") Integer id);

    List<Book> findBooksBySlugIn(Collection<String> slug);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "LEFT JOIN b.tags t " +
            "LEFT JOIN b.genres g " +
            "LEFT JOIN b.users b2u " +
            "LEFT JOIN b2u.user u " +
            "WHERE (u <> :user OR b2u.book IS NULL) AND (a IN :authors OR t IN :tags OR g IN :genres)")
    Page<Book> findBooksByTagsOrAuthorsOrGenres(@Param("tags") Set<Tag> tags,
                                                @Param("authors") List<Book2Author> authors,
                                                @Param("genres") Set<Genre> genres,
                                                @Param("user") User user,
                                                Pageable pageable);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.recentlyViews rv " +
            "LEFT JOIN rv.user u " +
            "WHERE u = :user ORDER BY rv.time DESC")
    Page<Book> findAllViewedBooksByUser(@Param("user") User user, Pageable pageable);
}
