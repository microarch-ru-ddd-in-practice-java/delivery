package model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateCourierResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-05T14:03:31.806648+03:00[Europe/Moscow]")
public class CreateCourierResponse {

  private UUID courierId;

  public CreateCourierResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateCourierResponse(UUID courierId) {
    this.courierId = courierId;
  }

  public CreateCourierResponse courierId(UUID courierId) {
    this.courierId = courierId;
    return this;
  }

  /**
   * Get courierId
   * @return courierId
  */
  @NotNull @Valid 
  @Schema(name = "courierId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("courierId")
  public UUID getCourierId() {
    return courierId;
  }

  public void setCourierId(UUID courierId) {
    this.courierId = courierId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateCourierResponse createCourierResponse = (CreateCourierResponse) o;
    return Objects.equals(this.courierId, createCourierResponse.courierId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(courierId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateCourierResponse {\n");
    sb.append("    courierId: ").append(toIndentedString(courierId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

