package com.joseguillard.my_blog.model;

import com.joseguillard.my_blog.model.enums.UserRole;
import com.joseguillard.my_blog.model.vo.Email;
import com.joseguillard.my_blog.model.vo.Slug;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false,  unique = true)
    private Email email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", unique = true, nullable = false))
    private Slug slug;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String avatarUrl;

    private String website;

    private String github;

    private String twitter;

    private String linkedin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.AUTHOR;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "author", cascade =  CascadeType.ALL)
    private Set<Post> posts = new HashSet<>();
}
