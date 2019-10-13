package revolut.backendtest.util;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.MoreObjects;

// poor man's single field data class :)
public abstract class LongValueObject {

  @JsonValue
  public final long value;

  protected LongValueObject(long value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LongValueObject that = (LongValueObject) o;

    return value == that.value;
  }

  @Override
  public int hashCode() {
    return (int) (value ^ value >>> 32);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("value", value)
        .toString();
  }
}

