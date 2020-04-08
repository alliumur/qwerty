package pl.rmv.qwerty.model;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Pole nie może być puste")
    @Length(max = 2048, message = "Wiadomość musi być krótrza")
    private String text;

    @NotBlank(message = "Pole nie może być puste")
    @Length(max = 16, message = "Tag musi być krótrzy")
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // ponieważ domyślnie była by nazwa z pola - 'author_id'
    private User author;

    private String filename;

    public Message() { }

    public Message(String text, String tag, User author, String filename) {
        this.text = text;
        this.tag = tag;
        this.author = author;
        this.filename = filename;
    }

    public String getAuthorName(){
        return author != null ? author.getUsername() : "<none>";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public User getAuthor() { return author; }

    public void setAuthor(User author) { this.author = author; }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
