package com.riancd.io.pulsar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tb_news")
@Data
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // gera os ID automaticamente
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT") // O tipo TEXT permite textos longos
    private String rawContent;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String sentiment;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

}
