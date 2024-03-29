package FirstLibraryProject.services;

import FirstLibraryProject.models.Book;
import FirstLibraryProject.models.Person;
import FirstLibraryProject.repositories.BookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepo bookRepo;

    @Autowired
    public BookService(BookRepo bookRepo) {
        this.bookRepo = bookRepo;
    }

    public List<Book> findAll(boolean sortByYear){
        if(sortByYear)
            return bookRepo.findAll(Sort.by("year"));
        else
            return bookRepo.findAll();
    }

    public List<Book> findWithPagination(Integer page, Integer bookPerPage, boolean sortByYear){
        if(sortByYear)
            return bookRepo.findAll(PageRequest.of(page, bookPerPage, Sort.by("year"))).getContent();
        else
            return bookRepo.findAll(PageRequest.of(page, bookPerPage)).getContent();
    }

    public Book findById(int id){
        Optional<Book> book = bookRepo.findById(id);
        return book.orElse(null);
    }

    public List<Book> searchByTitle(String query){
        return bookRepo.findByTitleStartingWith(query);
    }

    @Transactional
    public void save(Book book){
        bookRepo.save(book);
    }

    @Transactional
    public void update(Book updatedBook, int id){
        Book book = bookRepo.findById(id).get();

        updatedBook.setId(id);
        updatedBook.setOwner(book.getOwner());
        bookRepo.save(updatedBook);
    }

    @Transactional
    public void delete(int id){
        bookRepo.deleteById(id);
    }

    // Returns null if book has no owner
    public Person getBookOwner(int id) {
        // Здесь Hibernate.initialize() не нужен, так как владелец (сторона One) загружается не лениво
        return bookRepo.findById(id).map(Book::getOwner).orElse(null);
    }

    // Освбождает книгу (этот метод вызывается, когда человек возвращает книгу в библиотеку)
    @Transactional
    public void release(int id) {
        bookRepo.findById(id).ifPresent(
                book -> {
                    book.setOwner(null);
                    book.setTakenAt(null);
                });
    }

    // Назначает книгу человеку (этот метод вызывается, когда человек забирает книгу из библиотеки)
    @Transactional
    public void assign(int id, Person selectedPerson) {
        bookRepo.findById(id).ifPresent(
                book -> {
                    book.setOwner(selectedPerson);
                    book.setTakenAt(new Date()); // текущее время
                }
        );
    }
}
