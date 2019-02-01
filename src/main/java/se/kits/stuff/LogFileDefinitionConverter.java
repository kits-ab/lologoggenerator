package se.kits.stuff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.kits.stuff.model.LogFileDefinition;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.io.IOException;

@javax.faces.convert.FacesConverter(forClass = LogFileDefinition.class, value = "LogFileDefinitionConverter")
public class LogFileDefinitionConverter implements Converter {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, LogFileDefinition.class);
        } catch (IOException e) {
            throw new ConverterException(new FacesMessage(value + " is not a valid Logfiledefinition"), e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ConverterException(new FacesMessage(value + " is not a valid Logfiledefinition"), e);
        }
    }
}
