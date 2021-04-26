package org.home.syncBox.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "box_files")
@Data
public class BoxFile extends BaseEntity {

    @Column(name = "original_filename")
    String originalName;

    @Column(name = "name", unique = true)
    String name;

    @Column(name = "hash")
    String hash;

    @Column(name = "size")
    Long size;

    @Column(name = "human_size")
    String humanSize;

    @Column(name = "location")
    String location;

    @JsonIgnore
    @OneToOne
    User user;

}
