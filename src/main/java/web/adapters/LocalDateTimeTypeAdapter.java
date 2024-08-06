package web.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (Objects.isNull(localDateTime)) {
            jsonWriter.value("");
        } else {
            jsonWriter.value(localDateTime.format(DATE_TIME_FORMATTER));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String localDateTime = jsonReader.nextString();

        if (localDateTime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(localDateTime, DATE_TIME_FORMATTER);
    }
}
