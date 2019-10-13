package revolut.backendtest.api.spark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.UncheckedIOException;
import spark.ResponseTransformer;

public class JsonResponseTransformer implements ResponseTransformer {

  private final ObjectMapper mapper;

  @Inject
  public JsonResponseTransformer(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public String render(Object model) {
    try {
      return mapper.writeValueAsString(model);
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
