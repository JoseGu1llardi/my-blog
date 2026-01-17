package com.joseguillard.my_blog.model;

import com.joseguillard.my_blog.model.vo.Slug;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_slug", columnList = "slug"),
        @Index(name = "idx_post_status", columnList = "status"),
        @Index(name = "idx_post_published_at", columnList = "publishedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "slug", unique = true, nullable = false))
    private Slug slug;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    private String featuredImage;

}