package de.itdesign.application.data;

import lombok.Builder;
import lombok.Data;

/**
 * Class based on operations.xml
 */
@Builder
@Data
public class Operation {
    private String name;
    private String type;
    private String function;
    private String attribute;
    private String filter;
}
