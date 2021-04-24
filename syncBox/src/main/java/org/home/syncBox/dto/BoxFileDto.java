package org.home.syncBox.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoxFileDto {

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("size")
    private Long size;


}
