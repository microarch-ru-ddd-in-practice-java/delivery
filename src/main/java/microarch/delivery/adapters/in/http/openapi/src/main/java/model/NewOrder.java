package model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import model.Address;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * NewOrder
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-05T14:03:31.806648+03:00[Europe/Moscow]")
public class NewOrder {

  private UUID id;

  private Address address;

  private Integer volume;

  public NewOrder() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public NewOrder(UUID id, Address address, Integer volume) {
    this.id = id;
    this.address = address;
    this.volume = volume;
  }

  public NewOrder id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Идентификатор
   * @return id
  */
  @NotNull @Valid 
  @Schema(name = "id", description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public NewOrder address(Address address) {
    this.address = address;
    return this;
  }

  /**
   * Адрес
   * @return address
  */
  @NotNull @Valid 
  @Schema(name = "address", description = "Адрес", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("address")
  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public NewOrder volume(Integer volume) {
    this.volume = volume;
    return this;
  }

  /**
   * Объем
   * minimum: 0
   * @return volume
  */
  @NotNull @Min(0) 
  @Schema(name = "volume", description = "Объем", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("volume")
  public Integer getVolume() {
    return volume;
  }

  public void setVolume(Integer volume) {
    this.volume = volume;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NewOrder newOrder = (NewOrder) o;
    return Objects.equals(this.id, newOrder.id) &&
        Objects.equals(this.address, newOrder.address) &&
        Objects.equals(this.volume, newOrder.volume);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, address, volume);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewOrder {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    volume: ").append(toIndentedString(volume)).append("\n");
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

