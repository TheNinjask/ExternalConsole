package pt.theninjask.externalconsole.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Utils {

    public static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

}
