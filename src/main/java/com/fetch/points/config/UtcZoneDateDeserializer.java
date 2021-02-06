package com.fetch.points.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class UtcZoneDateDeserializer extends JsonDeserializer<ZonedDateTime> {

   @Override
   public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      final String dateFieldText = parser.getText();
      ZonedDateTime result;
      result = parseOutright(dateFieldText);
      if (result == null) {
         result = parseInDateTimeFormat(dateFieldText, DateTimeFormatter.ISO_ZONED_DATE_TIME);
      }
      if (result == null) {
         result = parseInDateTimeFormat(dateFieldText, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }
      if (result == null) {
         throw new DateTimeParseException("Unable to parse datetime string", dateFieldText, 0);
      }
      return result;
   }

   private ZonedDateTime parseOutright(final String dateFieldText) {
      try {
         return ZonedDateTime.parse(dateFieldText).with(ZoneOffset.UTC);
      } catch(DateTimeParseException ex) {
         log.debug(String.format("Unable to parse ZoneDataTime from %s", dateFieldText));
         return null;
      }
   }

   private ZonedDateTime parseInDateTimeFormat(final String dateFieldText, final DateTimeFormatter format) {
      try {
         return ZonedDateTime.parse(dateFieldText, format).with(ZoneOffset.UTC);
      } catch(DateTimeParseException ex) {
         log.debug(String.format("Unable to parse %s in %s", dateFieldText, format));
         return null;
      }
   }

}
