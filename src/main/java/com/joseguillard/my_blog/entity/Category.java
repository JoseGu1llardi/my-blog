package com.joseguillard.my_blog.entity;

import com.joseguillard.my_blog.entity.vo.Slug;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", unique = true, nullable = false)
    private Slug  slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String icon; // emoji or emoji's name

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Post> posts = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (slug == null && name != null) {
            slug = Slug.fromTitle(name);
        }
    }

    public int getPostCount() {
        return posts.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
