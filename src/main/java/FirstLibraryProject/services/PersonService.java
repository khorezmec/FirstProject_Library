package FirstLibraryProject.services;

import FirstLibraryProject.models.Book;
import FirstLibraryProject.models.Person;
import FirstLibraryProject.repositories.PersonRepo;
import jakarta.servlet.http.PushBuilder;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepo personRepo;

    @Autowired
    public PersonService(PersonRepo personRepo) {
        this.personRepo = personRepo;
    }


    public List<Person> findAll(){
        return personRepo.findAll();
    }

    public Person findById(int id){
        Optional<Person> person = personRepo.findById(id);
        return person.orElse(null);
    }

    @Transactional
    public void save(Person person){
        personRepo.save(person);
    }

    @Transactional
    public void update(Person person, int id){
        person.setId(id);
        personRepo.save(person);
    }

    @Transactional
    public void delete(int id){
        personRepo.deleteById(id);
    }

    public Optional<Person> findByFullName(String fullName){
        return personRepo.findByFullName(fullName);
    }

    public List<Book> getBooksByPersonId(int id) {
        Optional<Person> person = personRepo.findById(id);

        if (person.isPresent()) {
            Hibernate.initialize(person.get().getBooks());
            // Мы внизу итерируемся по книгам, поэтому они точно будут загружены, но на всякий случай
            // не мешает всегда вызывать Hibernate.initialize()
            // (на случай, например, если код в дальнейшем поменяется и итерация по книгам удалится)

            // Проверка просроченности книг
            person.get().getBooks().forEach(book -> {
                long diffInMillies = Math.abs(book.getTakenAt().getTime() - new Date().getTime());
                // 864000000 милисекунд = 10 суток
                if (diffInMillies > 864000000)
                    book.setExpired(true); // книга просрочена
            });

            return person.get().getBooks();
        }
        else {
            return Collections.emptyList();
        }
    }
}
