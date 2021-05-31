package com.example.springresthelloworld;

import com.example.springresthelloworld.error.BookNotFoundException;
import com.example.springresthelloworld.error.BookUnSupportedFieldPatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api")
public class BookController {

    @Autowired
    private BookRepository repository;

    @GetMapping("books")
    List<Book> findAll() {
        return repository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("books")
    Book newBook(@Valid @RequestBody Book newBook) {
        return repository.save(newBook);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("books/{id}")
    Book findOne(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("books/{id}")
    Book saveOrUpdate(@RequestBody Book newBook, @PathVariable Long id) {
        return repository.findById(id)
                .map(x -> {
                    x.setName(newBook.getName());
                    x.setAuthor(newBook.getAuthor());
                    x.setPrice(newBook.getPrice());
                    return repository.save(x);
                })
                .orElseGet(() -> {
                    newBook.setId(id);
                    return repository.save(newBook);
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("books/{id}")
    Book patch(@RequestBody Map<String, String> update, @PathVariable Long id) {
        return repository.findById(id)
                .map(x -> {
                    String author = update.get("author");
                    if (!StringUtils.isEmpty(author)) {
                        x.setAuthor(author);
                        return repository.save(x);
                    } else {
                        throw new BookUnSupportedFieldPatchException(update.keySet());
                    }
                })
                .orElseGet(() -> {
                    throw new BookNotFoundException(id);
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("books/{id}")
    void deleteBook(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
