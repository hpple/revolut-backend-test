package revolut.backendtest.api.spark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import revolut.backendtest.api.BadRequest;
import spark.Request;

public class JsonRequestTransformer implements RequestTransformer {

  private final ObjectMapper mapper;

  @Inject
  public JsonRequestTransformer(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public <T> T transform(Request request, Class<T> targetClass) {
    try {
      return mapper.readValue(request.body(), targetClass);
    } catch (JsonProcessingException ex) {
      throw new BadRequest("invalid input: " + ex.getMessage(), ex);
    }  catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
