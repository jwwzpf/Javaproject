package de.itdesign.application.data;

import lombok.Builder;
import lombok.Data;

/**
 * Class based on data.xml
 */
@Builder
@Data
public class City {
    private String name;
    private long population;
    private double area;
}
